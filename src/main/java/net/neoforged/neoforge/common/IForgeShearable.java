package net.neoforged.neoforge.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Minimal compatibility marker for legacy shearing hooks.
 */
public interface IForgeShearable {
  default boolean isShearable(ItemStack stack, Level level, BlockPos pos) {
    return false;
  }

  default List<ItemStack> onSheared(Player player, ItemStack stack, Level level, BlockPos pos, int fortune) {
    return List.of();
  }
}
