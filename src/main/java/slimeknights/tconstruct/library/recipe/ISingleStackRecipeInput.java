package slimeknights.tconstruct.library.recipe;

import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.recipe.container.ISingleStackContainer;

/** Single-stack container that is also a recipe input for 1.21 recipe bounds. */
public interface ISingleStackRecipeInput extends ISingleStackContainer, RecipeInput {
  @Override
  default ItemStack getItem(int index) {
    return ISingleStackContainer.super.getItem(index);
  }

  @Override
  default boolean isEmpty() {
    return ISingleStackContainer.super.isEmpty();
  }

  @Override
  default int size() {
    return getContainerSize();
  }
}
