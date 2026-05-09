package slimeknights.tconstruct.library.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import slimeknights.mantle.recipe.container.IEmptyContainer;

/** Empty container bridged to RecipeInput for 1.21 recipe bounds. */
public interface IEmptyRecipeInput extends IEmptyContainer, RecipeInput {
  @Override
  default ItemStack getItem(int index) {
    return IEmptyContainer.super.getItem(index);
  }

  @Override
  default boolean isEmpty() {
    return IEmptyContainer.super.isEmpty();
  }

  @Override
  default int size() {
    return 0;
  }
}
