package net.neoforged.neoforge.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

/**
 * Minimal compatibility interface used by old forge plant hooks.
 */
public interface IPlantable {
  PlantType getPlantType(BlockGetter world, BlockPos pos);
}
