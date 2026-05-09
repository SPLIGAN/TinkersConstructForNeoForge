package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.crafting.Ingredient;

/** Parses {@link Ingredient} from JSON using the vanilla / NeoForge codec stack. */
public final class TConstructIngredientJson {
  public static final String NEOFORGE_INGREDIENT_TYPE = "neoforge:ingredient_type";

  private TConstructIngredientJson() {}

  public static Ingredient parse(JsonElement element) {
    return Ingredient.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow(JsonParseException::new);
  }
}
