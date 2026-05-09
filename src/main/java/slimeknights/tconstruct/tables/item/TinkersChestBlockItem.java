package slimeknights.tconstruct.tables.item;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.util.BlockEntityHelper;
import slimeknights.tconstruct.library.utils.ItemStackTagCompat;
import slimeknights.tconstruct.tables.block.entity.chest.TinkersChestBlockEntity;

import javax.annotation.Nullable;

/** Dyeable chest block */
public class TinkersChestBlockItem extends BlockItem {
  public TinkersChestBlockItem(Block blockIn, Properties builder) {
    super(blockIn, builder);
  }

  public int getColor(ItemStack stack) {
    CompoundTag root = ItemStackTagCompat.getTag(stack);
    CompoundTag tag = root != null ? root.getCompound("display") : null;
    return tag != null && tag.contains("color", Tag.TAG_ANY_NUMERIC) ? tag.getInt("color") : TinkersChestBlockEntity.DEFAULT_COLOR;
  }

  public void setColor(ItemStack stack, int color) {
    CompoundTag root = ItemStackTagCompat.getOrCreateTag(stack);
    CompoundTag display = root.getCompound("display");
    display.putInt("color", color);
    root.put("display", display);
    ItemStackTagCompat.setTag(stack, root);
  }

  public boolean hasCustomColor(ItemStack stack) {
    CompoundTag root = ItemStackTagCompat.getTag(stack);
    CompoundTag tag = root != null ? root.getCompound("display") : null;
    return tag != null && tag.contains("color", Tag.TAG_ANY_NUMERIC);
  }

  @Override
  protected boolean updateCustomBlockEntityTag(BlockPos pos, Level worldIn, @Nullable Player player, ItemStack stack, BlockState state) {
    boolean result = super.updateCustomBlockEntityTag(pos, worldIn, player, stack, state);
    if (hasCustomColor(stack)) {
      int color = getColor(stack);
      BlockEntityHelper.get(TinkersChestBlockEntity.class, worldIn, pos).ifPresent(te -> te.setColor(color));
    }
    return result;
  }
}
