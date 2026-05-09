package slimeknights.mantle.recipe.helper;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.cooking.CookingResultRecipe;
import slimeknights.mantle.util.typed.TypedMapBuilder;

/** Compatibility helpers for RecordLoadable recipe serializers. */
public abstract class LoadableRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T> {
  public static final ContextKey<RecipeSerializer<?>> SERIALIZER = new ContextKey<>("serializer");
  public static final ContextKey<TypeAwareRecipeSerializer<?>> TYPED_SERIALIZER = new ContextKey<>("typed_serializer");
  public static final ContextKey<RecipeType<?>> TYPE = new ContextKey<>("type");
  public static final LoadableField<String, AbstractCookingRecipe> RECIPE_GROUP =
      StringLoadable.DEFAULT.defaultField("group", "", AbstractCookingRecipe::getGroup);

  protected final RecordLoadable<T> loadable;

  protected LoadableRecipeSerializer(RecordLoadable<T> loadable) {
    this.loadable = loadable;
  }

  @FunctionalInterface
  public interface CookingResultFactory<T extends AbstractCookingRecipe & CookingResultRecipe> {
    T create(String group, CookingBookCategory category, Ingredient ingredient, ItemOutput result, float experience, int cookingTime);
  }

  public static <T extends AbstractCookingRecipe & CookingResultRecipe> RecipeSerializer<T> cooking(CookingResultFactory<T> factory, int defaultCookingTime) {
    MapCodec<T> codec = RecordCodecBuilder.mapCodec(inst -> inst.group(
        Codec.STRING.optionalFieldOf("group", "").forGetter(AbstractCookingRecipe::getGroup),
        CookingBookCategory.CODEC.fieldOf("category").orElse(CookingBookCategory.MISC).forGetter(AbstractCookingRecipe::category),
        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(r -> r.getIngredients().getFirst()),
        ItemOutput.REQUIRED_STACK_CODEC.fieldOf("result").forGetter(CookingResultRecipe::getResult),
        Codec.FLOAT.fieldOf("experience").orElse(0f).forGetter(AbstractCookingRecipe::getExperience),
        Codec.INT.optionalFieldOf("cooking_time", defaultCookingTime).forGetter(AbstractCookingRecipe::getCookingTime)
    ).apply(inst, factory::create));

    StreamCodec<RegistryFriendlyByteBuf, T> streamCodec = StreamCodec.of(
        (buf, recipe) -> {
          buf.writeUtf(recipe.getGroup());
          buf.writeEnum(recipe.category());
          Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.getIngredients().getFirst());
          recipe.getResult().write(buf);
          buf.writeFloat(recipe.getExperience());
          buf.writeVarInt(recipe.getCookingTime());
        },
        buf -> factory.create(
            buf.readUtf(),
            buf.readEnum(CookingBookCategory.class),
            Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
            ItemOutput.read(buf),
            buf.readFloat(),
            buf.readVarInt()
        )
    );

    return new RecipeSerializer<>() {
      @Override
      public MapCodec<T> codec() {
        return codec;
      }

      @Override
      public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return streamCodec;
      }
    };
  }

  public static <T extends Recipe<?>> RecipeSerializer<T> of(RecordLoadable<T> loadable) {
    return new Impl<>(loadable);
  }

  public static <T extends Recipe<?>> TypeAwareRecipeSerializer<T> of(RecordLoadable<T> loadable, Supplier<RecipeType<? extends T>> type) {
    return new TypeAware<>(loadable, type);
  }

  public static <T extends Recipe<?>> RecipeSerializer<T> deprecated(RecordLoadable<T> loadable, String replacement) {
    return new Deprecated<>(loadable, replacement);
  }

  @SuppressWarnings("unchecked")
  protected static ResourceLocation recipeId(Recipe<?> recipe) {
    try {
      return (ResourceLocation) recipe.getClass().getMethod("getId").invoke(recipe);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Recipe missing getId(): " + recipe.getClass().getName(), e);
    }
  }

  protected TypedMapBuilder buildContext(ResourceLocation id) {
    return TypedMapBuilder.builder()
        .put(ContextKey.ID, id)
        .put(ContextKey.DEBUG, "Recipe " + id)
        .put(SERIALIZER, this);
  }

  protected MapCodec<T> codecImpl() {
    return new MapCodec<>() {
      @Override
      public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input) {
        try {
          O map = ops.createMap(input.entries());
          JsonObject json = GsonHelper.convertToJsonObject(ops.convertTo(JsonOps.INSTANCE, map), "Recipe");
          ResourceLocation id = ResourceLocation.parse(GsonHelper.getAsString(json, "id"));
          return DataResult.success(loadable.deserialize(json, buildContext(id).build()));
        } catch (RuntimeException ex) {
          return DataResult.error(ex::getMessage);
        }
      }

      @Override
      public <O> RecordBuilder<O> encode(T input, DynamicOps<O> ops, RecordBuilder<O> prefix) {
        JsonObject json = new JsonObject();
        loadable.serialize(input, json);
        json.addProperty("id", recipeId(input).toString());
        O encoded = JsonOps.INSTANCE.convertTo(ops, json);
        DataResult<Stream<Pair<O, O>>> values = ops.getMapValues(encoded);
        values.result().ifPresent(entries -> entries.forEach(entry -> prefix.add(entry.getFirst(), entry.getSecond())));
        return prefix;
      }

      @Override
      public <O> Stream<O> keys(DynamicOps<O> ops) {
        return Stream.empty();
      }
    };
  }

  @Override
  public MapCodec<T> codec() {
    return codecImpl();
  }

  @Override
  public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
    return StreamCodec.of(
        (buf, recipe) -> {
          ResourceLocation.STREAM_CODEC.encode(buf, recipeId(recipe));
          loadable.encode(buf, recipe);
        },
        buf -> {
          ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buf);
          return loadable.decode(buf, buildContext(id).build());
        }
    );
  }

  private static class Impl<T extends Recipe<?>> extends LoadableRecipeSerializer<T> {
    Impl(RecordLoadable<T> loadable) {
      super(loadable);
    }
  }

  private static class TypeAware<T extends Recipe<?>> extends LoadableRecipeSerializer<T> implements TypeAwareRecipeSerializer<T> {
    private final Supplier<RecipeType<? extends T>> type;

    TypeAware(RecordLoadable<T> loadable, Supplier<RecipeType<? extends T>> type) {
      super(loadable);
      this.type = type;
    }

    @Override
    protected TypedMapBuilder buildContext(ResourceLocation id) {
      return super.buildContext(id).put(TYPE, getType()).put(TYPED_SERIALIZER, this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeType<T> getType() {
      return (RecipeType<T>) (RecipeType<?>) type.get();
    }
  }

  private static class Deprecated<T extends Recipe<?>> extends LoadableRecipeSerializer<T> {
    private final String replacement;

    Deprecated(RecordLoadable<T> loadable, String replacement) {
      super(loadable);
      this.replacement = replacement;
    }

    @Override
    public MapCodec<T> codec() {
      MapCodec<T> base = super.codecImpl();
      return new MapCodec<>() {
        @Override
        public <O> DataResult<T> decode(DynamicOps<O> ops, MapLike<O> input) {
          return base.decode(ops, input).map(recipe -> {
            Mantle.logger.warn(
                "Using deprecated recipe serializer {} for recipe {}, {}",
                BuiltInRegistries.RECIPE_SERIALIZER.getKey(Deprecated.this),
                recipeId(recipe),
                replacement
            );
            return recipe;
          });
        }

        @Override
        public <O> RecordBuilder<O> encode(T input, DynamicOps<O> ops, RecordBuilder<O> prefix) {
          return base.encode(input, ops, prefix);
        }

        @Override
        public <O> Stream<O> keys(DynamicOps<O> ops) {
          return base.keys(ops);
        }
      };
    }
  }
}
