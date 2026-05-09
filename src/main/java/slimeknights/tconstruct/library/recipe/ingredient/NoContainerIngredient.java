package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.tconstruct.TConstruct;

/** Ingredient matching an item with no container item, used to ensure NBT fluid items are empty */
public class NoContainerIngredient extends NestedIngredient {
  public static final ResourceLocation ID = TConstruct.getResource("no_container");

  public static final MapCodec<NoContainerIngredient> MAP_CODEC = RecordCodecBuilder.mapCodec(inst ->
      inst.group(
          Ingredient.CODEC.fieldOf("match").forGetter(NoContainerIngredient::nested)
      ).apply(inst, NoContainerIngredient::new)
  );

  protected NoContainerIngredient(Ingredient nested) {
    super(nested);
  }

  @Override
  public boolean test(ItemStack stack) {
    return !stack.isEmpty() && super.test(stack) && !stack.hasCraftingRemainingItem();
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    return TinkerIngredientTypes.NO_CONTAINER.get();
  }

  /** Serializes for recipe JSON / datagen (NeoForge custom ingredient shape). */
  public JsonElement toJson() {
    JsonObject json = new JsonObject();
    json.addProperty(TConstructIngredientJson.NEOFORGE_INGREDIENT_TYPE, ID.toString());
    json.add("match", Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, nested()).getOrThrow());
    return json;
  }

  public static NoContainerIngredient parse(JsonObject json) {
    Ingredient ingredient;
    if (json.has("match")) {
      ingredient = TConstructIngredientJson.parse(json.get("match"));
    } else {
      JsonObject copy = json.deepCopy();
      copy.remove(TConstructIngredientJson.NEOFORGE_INGREDIENT_TYPE);
      copy.remove("type");
      ingredient = TConstructIngredientJson.parse(copy);
    }
    return new NoContainerIngredient(ingredient);
  }

  /* Static constructors — return wrapped vanilla {@link Ingredient} for recipe APIs */

  public static Ingredient of(Ingredient ingredient) {
    return new NoContainerIngredient(ingredient).toVanilla();
  }

  public static Ingredient of(ItemLike... items) {
    return of(Ingredient.of(items));
  }

  public static Ingredient of(ItemStack... stacks) {
    return of(Ingredient.of(stacks));
  }

  public static Ingredient of(TagKey<Item> tag) {
    return of(Ingredient.of(tag));
  }

  public static NoContainerIngredient unwrap(Ingredient ingredient) {
    if (ingredient.isCustom() && ingredient.getCustomIngredient() instanceof NoContainerIngredient no) {
      return no;
    }
    return null;
  }
}
