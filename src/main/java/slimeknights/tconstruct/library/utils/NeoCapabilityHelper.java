package slimeknights.tconstruct.library.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;

/** NeoForge block capability lookups (replaces BlockEntity#getCapability). */
public final class NeoCapabilityHelper {
  private NeoCapabilityHelper() {}

  @Nullable
  public static IFluidHandler getBlockFluid(Level level, BlockPos pos, @Nullable Direction context) {
    return level.getCapability(Capabilities.FluidHandler.BLOCK, pos, null, null, context);
  }

  public static LazyOptional<IFluidHandler> getBlockFluidLazy(Level level, BlockPos pos, @Nullable Direction context) {
    return LazyOptional.of(() -> getBlockFluid(level, pos, context));
  }

  @Nullable
  public static IItemHandler getBlockItem(Level level, BlockPos pos, @Nullable Direction context) {
    return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null, null, context);
  }

  public static LazyOptional<IItemHandler> getBlockItemLazy(Level level, BlockPos pos, @Nullable Direction context) {
    return LazyOptional.of(() -> getBlockItem(level, pos, context));
  }
}
