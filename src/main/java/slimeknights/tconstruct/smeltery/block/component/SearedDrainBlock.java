package slimeknights.tconstruct.smeltery.block.component;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import slimeknights.mantle.fluid.FluidTransferHelper;
import slimeknights.tconstruct.smeltery.block.entity.component.DrainBlockEntity;

/** Extension to include interaction behavior */
public class SearedDrainBlock extends RetexturedOrientableSmelteryBlock {
  public SearedDrainBlock(Properties properties) {
    super(properties, DrainBlockEntity::new);
  }

  @Override
  protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
    if (FluidTransferHelper.interactWithTank(level, pos, player, hand, hit.getDirection(), state.getValue(FACING).getOpposite())) {
      return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }
    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
  }

  @Override
  protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
    if (FluidTransferHelper.interactWithTank(level, pos, player, InteractionHand.MAIN_HAND, hit.getDirection(), state.getValue(FACING).getOpposite())) {
      return InteractionResult.sidedSuccess(level.isClientSide());
    }
    return InteractionResult.PASS;
  }
}
