package slimeknights.tconstruct.smeltery.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import slimeknights.mantle.block.entity.NameableBlockEntity;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.entity.inventory.HeaterItemHandler;
import slimeknights.tconstruct.smeltery.menu.SingleItemContainerMenu;

import javax.annotation.Nullable;

/** Tile entity for the heater block below the melter */
public class HeaterBlockEntity extends NameableBlockEntity {
  private static final String TAG_ITEM = "item";
  private static final Component TITLE = TConstruct.makeTranslation("gui", "heater");

  private final HeaterItemHandler itemHandler = new HeaterItemHandler(this);

  protected HeaterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state, TITLE);
  }

  public HeaterBlockEntity(BlockPos pos, BlockState state) {
    this(TinkerSmeltery.heater.get(), pos, state);
  }

  public IItemHandler getItemHandler() {
    return itemHandler;
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inventory, Player playerEntity) {
    return new SingleItemContainerMenu(id, inventory, this);
  }


  /* NBT */

  @Override
  public void loadAdditional(CompoundTag tags, HolderLookup.Provider provider) {
    super.loadAdditional(tags, provider);
    if (tags.contains(TAG_ITEM, Tag.TAG_COMPOUND)) {
      itemHandler.setStack(ItemStack.parse(provider, tags.getCompound(TAG_ITEM)).orElse(ItemStack.EMPTY));
    }
  }

  @Override
  public void saveAdditional(CompoundTag tags, HolderLookup.Provider provider) {
    super.saveAdditional(tags, provider);
    ItemStack stack = itemHandler.getStack();
    if (!stack.isEmpty()) {
      tags.put(TAG_ITEM, stack.save(provider, new CompoundTag()));
    }
  }
}
