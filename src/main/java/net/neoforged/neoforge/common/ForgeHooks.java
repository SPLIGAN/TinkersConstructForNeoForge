package net.neoforged.neoforge.common;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Minimal ForgeHooks compatibility shim for removed legacy APIs.
 */
public final class ForgeHooks {
  private ForgeHooks() {}

  public static float[] onLivingFall(LivingEntity entity, float distance, float damageMultiplier) {
    return new float[] {distance, damageMultiplier};
  }

  public static int getBurnTime(ItemStack stack, RecipeType<?> recipeType) {
    return stack.getBurnTime(recipeType);
  }

  public static void setCraftingPlayer(Player player) {
    // no-op compatibility hook
  }

  public static ItemStack getProjectile(LivingEntity entity, ItemStack weapon, ItemStack projectile) {
    return projectile;
  }
}
