package net.neoforged.neoforge.event;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

/**
 * Minimal ForgeEventFactory compatibility shim.
 */
public final class ForgeEventFactory {
  private ForgeEventFactory() {}

  public static void firePlayerCraftingEvent(Player player, ItemStack result, Container inventory) {
    // no-op
  }

  public static boolean onProjectileImpact(Projectile projectile, HitResult result) {
    return false;
  }

  public static void onPlayerDestroyItem(Player player, ItemStack stack, net.minecraft.world.InteractionHand hand) {
    // no-op
  }
}
