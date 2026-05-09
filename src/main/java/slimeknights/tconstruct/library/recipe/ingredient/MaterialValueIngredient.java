package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.util.typed.TypedMapBuilder;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.json.TinkerLoadables;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicate;
import slimeknights.tconstruct.library.json.predicate.material.MaterialPredicateField;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;

/**
 * Ingredient matching material items with the given value. Typically, matches ingots or blocks
 */
@Getter
@RequiredArgsConstructor
public class MaterialValueIngredient implements ICustomIngredient {
  private static final LoadableField<IJsonPredicate<MaterialVariantId>, MaterialValueIngredient> MATERIAL_FIELD =
      new MaterialPredicateField<>("material", i -> i.material);

  private final IJsonPredicate<MaterialVariantId> material;
  private final float minValue;
  private final float maxValue;
  private ItemStack[] items;

  /** Shared by {@link MaterialIngredient} for JSON / network codecs. */
  static final Codec<IJsonPredicate<MaterialVariantId>> MATERIAL_PREDICATE_CODEC =
      Codec.STRING.xmap(serialized -> (IJsonPredicate<MaterialVariantId>) MaterialPredicate.ANY, material -> "{}");

  private record ValueRange(float min, float max) {}

  private static final Codec<ValueRange> VALUE_RANGE_CODEC = Codec.STRING.xmap(
      serialized -> decodeValueRange(JsonParser.parseString(serialized)),
      vr -> encodeValueRange(vr).toString()
  );

  public static final MapCodec<MaterialValueIngredient> MAP_CODEC =
      MapCodec.unit(new MaterialValueIngredient(MaterialPredicate.ANY, 0, Float.POSITIVE_INFINITY));

  private static DataResult<IJsonPredicate<MaterialVariantId>> decodeMaterialPredicate(JsonElement element) {
    try {
      return DataResult.success(decodeMaterial(element));
    } catch (RuntimeException ex) {
      return DataResult.error(() -> "Failed to parse material predicate: " + ex.getMessage());
    }
  }

  private static IJsonPredicate<MaterialVariantId> decodeMaterial(JsonElement element) {
    var context = TypedMapBuilder.builder().build();
    if (element.isJsonPrimitive()) {
      return MaterialPredicate.variant(MaterialVariantId.LOADABLE.convert(element, "material", context));
    }
    return MaterialPredicate.LOADER.convert(element, "material", context);
  }

  private static DataResult<JsonElement> encodeMaterialPredicate(IJsonPredicate<MaterialVariantId> material) {
    JsonObject serialized = new JsonObject();
    MaterialPredicate.LOADER.serialize(material, serialized);
    return DataResult.success(serialized);
  }

  private static ValueRange decodeValueRange(JsonElement value) {
    if (value.isJsonPrimitive()) {
      float v = value.getAsFloat();
      return new ValueRange(v, v);
    }
    JsonObject object = GsonHelper.convertToJsonObject(value, "value");
    return new ValueRange(GsonHelper.getAsFloat(object, "min", 0), GsonHelper.getAsFloat(object, "max", Float.POSITIVE_INFINITY));
  }

  private static JsonElement encodeValueRange(ValueRange vr) {
    if (vr.min() == vr.max()) {
      return new JsonPrimitive(vr.min());
    }
    JsonObject o = new JsonObject();
    if (vr.min() > 0) {
      o.addProperty("min", vr.min());
    }
    if (Float.isFinite(vr.max())) {
      o.addProperty("max", vr.max());
    }
    return o;
  }

  /** Creates an ingredient matching a range of values */
  public static Ingredient of(IJsonPredicate<MaterialVariantId> materials, float minValue, float maxValue) {
    return new MaterialValueIngredient(materials, minValue, maxValue).toVanilla();
  }

  /** Creates an ingredient matching an exact value */
  public static Ingredient of(IJsonPredicate<MaterialVariantId> materials, float value) {
    return of(materials, value, value);
  }

  /** Checks the given material recipe against our filters */
  public boolean test(MaterialRecipe materialRecipe) {
    float value = materialRecipe.getValue() / (float) materialRecipe.getNeeded();
    return minValue <= value && value <= maxValue && this.material.matches(materialRecipe.getMaterial().getVariant());
  }

  @Override
  public boolean test(ItemStack stack) {
    MaterialRecipe recipe = MaterialRecipeCache.findRecipe(stack);
    return recipe != MaterialRecipe.EMPTY && test(recipe);
  }

  @Override
  public java.util.stream.Stream<ItemStack> getItems() {
    if (items == null) {
      items = MaterialRecipeCache.getAllRecipes().stream()
          .filter(this::test)
          .flatMap(materialRecipe -> Arrays.stream(materialRecipe.getIngredient().getItems()))
          .toArray(ItemStack[]::new);
    }
    return Arrays.stream(items);
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public IngredientType<?> getType() {
    return TinkerIngredientTypes.MATERIAL_VALUE.get();
  }

  /* Helpers for ShapedMaterialRecipe */

  /** Checks if this ingredient fully contains the range of the other */
  private boolean contains(MaterialValueIngredient other) {
    return this.minValue <= other.minValue && other.maxValue <= this.maxValue;
  }

  /** Creates an ingredient that matches anything either of the two ingredients matches */
  public MaterialValueIngredient merge(MaterialValueIngredient other) {
    if (this == other) return this;

    IJsonPredicate<MaterialVariantId> predicate = this.material;
    if (this.material.equals(other.material)) {
      if (this.contains(other)) {
        return this;
      }
      if (other.contains(this)) {
        return other;
      }
    } else {
      predicate = MaterialPredicate.or(this.material, other.material);
    }
    return new MaterialValueIngredient(predicate, Math.min(this.minValue, other.minValue), Math.max(this.maxValue, other.maxValue));
  }

  /** Gets the material matching this recipe */
  @Nullable
  public MaterialVariantId getMaterial(ItemStack stack) {
    MaterialRecipe recipe = MaterialRecipeCache.findRecipe(stack);
    return recipe != MaterialRecipe.EMPTY && test(recipe) ? recipe.getMaterial().getVariant() : null;
  }

  public JsonElement toJson() {
    JsonObject json = new JsonObject();
    json.addProperty(TConstructIngredientJson.NEOFORGE_INGREDIENT_TYPE, TConstruct.getResource("material_value").toString());
    MATERIAL_FIELD.serialize(this, json);
    if (minValue == maxValue) {
      json.addProperty("value", minValue);
    } else {
      JsonObject value = new JsonObject();
      if (minValue > 0) {
        value.addProperty("min", minValue);
      }
      if (Float.isFinite(maxValue)) {
        value.addProperty("max", maxValue);
      }
      json.add("value", value);
    }
    return json;
  }

  public static MaterialValueIngredient parse(JsonObject json) {
    float minValue;
    float maxValue;
    JsonElement value = json.get("value");
    if (value.isJsonPrimitive()) {
      minValue = maxValue = value.getAsJsonPrimitive().getAsFloat();
    } else {
      JsonObject object = GsonHelper.convertToJsonObject(value, "value");
      minValue = GsonHelper.getAsFloat(object, "min", 0);
      maxValue = GsonHelper.getAsFloat(object, "max", Float.POSITIVE_INFINITY);
    }
    IJsonPredicate<MaterialVariantId> mat = MATERIAL_FIELD.get(json);
    if (json.has("tag")) {
      TConstruct.LOG.warn("Using deprecated tag field on material value ingredient");
      IJsonPredicate<MaterialVariantId> tagPredicate = MaterialPredicate.tag(
          TinkerLoadables.MATERIAL_TAGS.getIfPresent(json, "tag"));
      if (mat == MaterialPredicate.ANY) {
        mat = tagPredicate;
      } else {
        mat = MaterialPredicate.and(mat, tagPredicate);
      }
    }
    return new MaterialValueIngredient(mat, minValue, maxValue);
  }

  public static MaterialValueIngredient unwrap(Ingredient ingredient) {
    if (ingredient.isCustom() && ingredient.getCustomIngredient() instanceof MaterialValueIngredient m) {
      return m;
    }
    return null;
  }
}
