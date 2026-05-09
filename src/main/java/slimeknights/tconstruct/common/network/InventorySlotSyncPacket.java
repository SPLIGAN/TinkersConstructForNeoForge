package slimeknights.tconstruct.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.packet.IThreadsafePacket;

/** Minimal compatibility packet for server-focused build. */
public class InventorySlotSyncPacket implements IThreadsafePacket {
  public final ItemStack itemStack;
  public final int slot;
  public final BlockPos pos;

  public InventorySlotSyncPacket(ItemStack itemStack, int slot, BlockPos pos) {
    this.itemStack = itemStack;
    this.slot = slot;
    this.pos = pos;
  }

  public InventorySlotSyncPacket(FriendlyByteBuf buffer) {
    this(ItemStack.EMPTY, 0, BlockPos.ZERO);
  }

  @Override
  public void encode(FriendlyByteBuf packetBuffer) {}

  @Override
  public void handleThreadsafe(IPayloadContext context) {}
}
