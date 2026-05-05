package slimeknights.tconstruct.library.data.recipe;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;

/** Temporary 1.21 shim: NBT-wrapped crafting output currently forwards unchanged recipes. */
public final class CraftingNBTWrapper {
  private CraftingNBTWrapper() {}

  public static RecipeOutput wrap(RecipeOutput base, CompoundTag nbt) {
    return base;
  }
}
