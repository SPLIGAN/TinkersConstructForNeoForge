package slimeknights.tconstruct.library.recipe.casting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.item.crafting.RecipeInput;
import slimeknights.mantle.recipe.container.ISingleStackContainer;

import javax.annotation.Nullable;

/**
 * Inventory containing a single item and a fluid
 */
public interface ICastingContainer extends ISingleStackContainer, RecipeInput {
  /**
   * Gets the contained fluid in this inventory
   * @return  Contained fluid
   */
  Fluid getFluid();

  /**
   * Gets the NBT for the contained fluid
   * @return  Fluid's NBT
   */
  @Nullable
  default CompoundTag getFluidTag() {
    return null;
  }

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
