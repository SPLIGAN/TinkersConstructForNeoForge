package slimeknights.tconstruct.plugin.jei.util;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;

import javax.annotation.Nullable;

/** Common logic for subtype interpreter between the fluid and item form of our potion. Based on a JEI class with the same name */
public interface PotionSubtypeInterpreter<T> extends IIngredientSubtypeInterpreter<T> {
  @Nullable
  String getSubtype(T ingredient);

  @Override
  default String apply(T ingredient, UidContext context) {
    String subtype = getSubtype(ingredient);
    if (subtype == null || subtype.isEmpty()) {
      return IIngredientSubtypeInterpreter.NONE;
    }
    return subtype;
  }
}
