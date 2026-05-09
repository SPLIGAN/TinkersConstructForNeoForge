package slimeknights.tconstruct.library.recipe.fuel;

import net.minecraft.world.level.material.Fluid;
import slimeknights.tconstruct.library.recipe.IEmptyRecipeInput;

/**
 * Inventory containing just a single fluid
 */
public interface IFluidContainer extends IEmptyRecipeInput {
  /**
   * Gets the fluid contained in this inventory
   * @return  Contained fluid
   */
  Fluid getFluid();

}
