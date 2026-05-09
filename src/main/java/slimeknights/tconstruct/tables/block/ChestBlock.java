package slimeknights.tconstruct.tables.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.IItemHandler;
import slimeknights.tconstruct.library.utils.ItemStackTagCompat;
import slimeknights.tconstruct.tables.block.entity.chest.AbstractChestBlockEntity;

import javax.annotation.Nullable;

/**
 * Shared block logic for all chest types
 */
public class ChestBlock extends TabbedTableBlock {
  private static final VoxelShape SHAPE = Shapes.or(
    Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D), //top
    Block.box(1.0D, 3.0D, 1.0D, 15.0D, 16.0D, 15.0D), //middle
    Block.box(0.5D, 0.0D, 0.5D, 2.5D, 15.0D, 2.5D), //leg
    Block.box(13.5D, 0.0D, 0.5D, 15.5D, 15.0D, 2.5D), //leg
    Block.box(13.5D, 0.0D, 13.5D, 15.5D, 15.0D, 15.5D), //leg
    Block.box(0.5D, 0.0D, 13.5D, 2.5D, 15.0D, 15.5D) //leg
                                                        );

  private final BlockEntitySupplier<? extends BlockEntity> blockEntity;
  private final boolean dropsItems;
  public ChestBlock(Properties builder, BlockEntitySupplier<? extends BlockEntity> blockEntity, boolean dropsItems) {
    super(builder);
    this.blockEntity = blockEntity;
    this.dropsItems = dropsItems;
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
    return blockEntity.create(pPos, pState);
  }

  @Override
  public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(worldIn, pos, state, placer, stack);
    // check if we also have an inventory

    CompoundTag tag = ItemStackTagCompat.getTag(stack);
    if (tag != null && tag.contains("TinkerData", Tag.TAG_COMPOUND)) {
      CompoundTag tinkerData = tag.getCompound("TinkerData");
      BlockEntity te = worldIn.getBlockEntity(pos);
      if (te instanceof AbstractChestBlockEntity chest) {
        chest.readInventory(tinkerData, worldIn.registryAccess());
      }
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  @Deprecated
  public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
    return SHAPE;
  }

  @Override
  protected void dropInventoryItems(BlockState state, Level worldIn, BlockPos pos, IItemHandler inventory) {
    if (dropsItems) {
      dropInventoryItems(worldIn, pos, inventory);
    }
  }
}
