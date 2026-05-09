package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.stream.Stream;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.util.typed.TypedMapBuilder;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.json.TinkerLoadables;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicateField;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.library.utils.ItemStackTagCompat;

import javax.annotation.Nullable;

/**
 * Extension of the vanilla ingredient to display materials on items and support matching by materials
 */
public class MaterialIngredient extends NestedIngredient {
  private static final LoadableField<IJsonPredicate<MaterialVariantId>, MaterialIngredient> MATERIAL_FIELD =
      new MaterialPredicateField<>("material", i -> i.material);

  private final IJsonPredicate<MaterialVariantId> material;
  @Nullable
  private ItemStack[] materialStacks;

  public static final MapCodec<MaterialIngredient> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
      Ingredient.CODEC.fieldOf("match").forGetter(MaterialIngredient::nested),
      MaterialValueIngredient.MATERIAL_PREDICATE_CODEC.optionalFieldOf("material", MaterialPredicate.ANY).forGetter(m -> m.material)
  ).apply(inst, MaterialIngredient::new));

  protected MaterialIngredient(Ingredient nested, IJsonPredicate<MaterialVariantId> material) {
    super(nested);
    this.material = material;
  }

  /** @deprecated use {@link #MaterialIngredient(Ingredient, IJsonPredicate)} */
  @Deprecated(forRemoval = true)
  protected MaterialIngredient(Ingredient nested, MaterialVariantId material, @Nullable TagKey<IMaterial> tag) {
    this(nested, makePredicate(material, tag));
  }

  /** Converts the legacy material and tag into a predicate */
  private static IJsonPredicate<MaterialVariantId> makePredicate(MaterialVariantId material, @Nullable TagKey<IMaterial> tag) {
    IJsonPredicate<MaterialVariantId> predicate = material.equals(IMaterial.UNKNOWN.getIdentifier()) ? MaterialPredicate.ANY : MaterialPredicate.variant(material);
    if (tag != null) {
      IJsonPredicate<MaterialVariantId> tagPredicate = MaterialPredicate.tag(tag);
      if (predicate == MaterialPredicate.ANY) {
        predicate = tagPredicate;
      } else {
        predicate = MaterialPredicate.and(predicate, tagPredicate);
      }
    }
    return predicate;
  }

  public static Ingredient of(Ingredient ingredient, IJsonPredicate<MaterialVariantId> material) {
    return new MaterialIngredient(ingredient, material).toVanilla();
  }

  public static Ingredient of(ItemLike item, IJsonPredicate<MaterialVariantId> material) {
    return of(Ingredient.of(item), material);
  }

  public static Ingredient of(Ingredient ingredient) {
    return new MaterialIngredient(ingredient, MaterialPredicate.ANY).toVanilla();
  }

  public static Ingredient of(Ingredient ingredient, MaterialVariantId material) {
    return of(ingredient, MaterialPredicate.variant(material));
  }

  public static Ingredient of(Ingredient ingredient, TagKey<IMaterial> tag) {
    return of(ingredient, MaterialPredicate.tag(tag));
  }

  public static Ingredient of(ItemLike item, MaterialVariantId material) {
    return of(Ingredient.of(item), material);
  }

  public static Ingredient of(ItemLike item, TagKey<IMaterial> tag) {
    return of(Ingredient.of(item), tag);
  }

  public static Ingredient of(ItemLike item) {
    return of(Ingredient.of(item));
  }

  public static Ingredient of(TagKey<Item> tag, MaterialVariantId material) {
    return of(Ingredient.of(tag), material);
  }

  public static Ingredient of(TagKey<Item> tag) {
    return of(Ingredient.of(tag));
  }

  @Override
  public boolean test(ItemStack stack) {
    if (stack.isEmpty() || !super.test(stack)) {
      return false;
    }
    if (material != MaterialPredicate.ANY) {
      return material.matches(IMaterialItem.getMaterialFromStack(stack));
    }
    return true;
  }

  @Override
  public Stream<ItemStack> getItems() {
    if (materialStacks == null) {
      if (!MaterialRegistry.isFullyLoaded()) {
        return Arrays.stream(nested().getItems());
      }
      Stream<ItemStack> items = Arrays.stream(nested().getItems());
      items = items.flatMap(stack -> MaterialRecipeCache.getAllVariants().stream()
          .filter(material::matches)
          .map(mat -> IMaterialItem.withMaterial(stack, mat))
          .filter(it -> ItemStackTagCompat.getTag(it) != null));
      materialStacks = items.distinct().toArray(ItemStack[]::new);
    }
    return Arrays.stream(materialStacks);
  }

  public JsonElement toJson() {
    JsonElement parent = Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, nested()).getOrThrow();
    JsonObject result = new JsonObject();
    result.add("match", parent);
    result.addProperty(TConstructIngredientJson.NEOFORGE_INGREDIENT_TYPE, TConstruct.getResource("material").toString());
    MATERIAL_FIELD.serialize(this, result);
    return result;
  }

  private void invalidate() {
    this.materialStacks = null;
  }

  @Override
  public boolean isSimple() {
    return material == MaterialPredicate.ANY;
  }

  @Override
  public IngredientType<?> getType() {
    return TinkerIngredientTypes.MATERIAL.get();
  }

  public static MaterialIngredient parse(JsonObject json) {
    Ingredient ingredient;
    if (json.has("match")) {
      ingredient = TConstructIngredientJson.parse(json.get("match"));
    } else {
      JsonObject copy = json.deepCopy();
      copy.remove("material");
      copy.remove("tag");
      copy.remove("type");
      copy.remove(TConstructIngredientJson.NEOFORGE_INGREDIENT_TYPE);
      ingredient = TConstructIngredientJson.parse(copy);
    }
    IJsonPredicate<MaterialVariantId> mat = MATERIAL_FIELD.get(json);
    if (json.has("tag")) {
      TConstruct.LOG.warn("Using deprecated tag field on material ingredient");
      IJsonPredicate<MaterialVariantId> tagPredicate = MaterialPredicate.tag(TinkerLoadables.MATERIAL_TAGS.getIfPresent(json, "tag"));
      if (mat == MaterialPredicate.ANY) {
        mat = tagPredicate;
      } else {
        mat = MaterialPredicate.and(mat, tagPredicate);
      }
    }
    return new MaterialIngredient(ingredient, mat);
  }

  public static MaterialIngredient readNetwork(FriendlyByteBuf buffer) {
    Ingredient ingredient = TConstructIngredientJson.parse(JsonParser.parseString(buffer.readUtf()));
    return new MaterialIngredient(
        ingredient,
        MaterialPredicate.LOADER.decode(buffer, TypedMapBuilder.builder().build())
    );
  }

  public void writeNetwork(FriendlyByteBuf buffer) {
    JsonElement json = Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, nested()).getOrThrow();
    buffer.writeUtf(json.toString());
    MaterialPredicate.LOADER.encode(buffer, material);
  }

  public static MaterialIngredient unwrap(Ingredient ingredient) {
    if (ingredient.isCustom() && ingredient.getCustomIngredient() instanceof MaterialIngredient m) {
      return m;
    }
    return null;
  }
}
