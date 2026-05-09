package slimeknights.tconstruct.world.block;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.IForgeShearable;

import slimeknights.tconstruct.world.TinkerWorld;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class SlimeTallGrassBlock extends BushBlock implements IForgeShearable {

  private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

  public static final MapCodec<SlimeTallGrassBlock> DIRECT_CODEC =
      RecordCodecBuilder.mapCodec(
          inst ->
              inst.group(
                      propertiesCodec(),
                      StringRepresentable.fromEnum(FoliageType::values)
                          .fieldOf("foliage_type")
                          .forGetter(SlimeTallGrassBlock::getFoliageType))
                  .apply(inst, SlimeTallGrassBlock::new));

  @Getter
  private final FoliageType foliageType;
  public SlimeTallGrassBlock(Properties properties, FoliageType foliageType) {
    super(properties);
    this.foliageType = foliageType;
  }

  /** For structure/worldgen callers that cannot access {@link BushBlock#canSurvive}. */
  public boolean canSurviveForWorldgen(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
    return super.canSurvive(state, level, pos);
  }

  @Override
  protected MapCodec<? extends BushBlock> codec() {
    return DIRECT_CODEC;
  }

  @Override
  protected VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
    return SHAPE;
  }

  @Nonnull
  @Override
  public List<ItemStack> onSheared(@Nullable Player player, ItemStack item, Level world, BlockPos pos, int fortune) {
    return Lists.newArrayList(new ItemStack(this, 1));
  }

  @Override
  protected boolean mayPlaceOn(BlockState state, BlockGetter worldIn, BlockPos pos) {
    Block block = state.getBlock();
    return TinkerWorld.slimeDirt.contains(block) || TinkerWorld.vanillaSlimeGrass.contains(block) || TinkerWorld.earthSlimeGrass.contains(block) || TinkerWorld.skySlimeGrass.contains(block) || TinkerWorld.enderSlimeGrass.contains(block) || TinkerWorld.ichorSlimeGrass.contains(block);
  }
}
