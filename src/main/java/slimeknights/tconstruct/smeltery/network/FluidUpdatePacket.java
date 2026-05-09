package slimeknights.tconstruct.smeltery.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.mantle.util.BlockEntityHelper;

public class FluidUpdatePacket implements IThreadsafePacket {

  protected final BlockPos pos;
  protected final FluidStack fluid;

  public FluidUpdatePacket(BlockPos pos, FluidStack fluid) {
    this.pos = pos;
    this.fluid = fluid;
  }

  public FluidUpdatePacket(FriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
    ResourceLocation fluidId = buffer.readResourceLocation();
    int amount = buffer.readVarInt();
    if (amount <= 0) {
      this.fluid = FluidStack.EMPTY;
    } else {
      this.fluid = new FluidStack(BuiltInRegistries.FLUID.get(fluidId), amount);
    }
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBlockPos(pos);
    if (fluid.isEmpty()) {
      buffer.writeResourceLocation(ResourceLocation.fromNamespaceAndPath("minecraft", "empty"));
      buffer.writeVarInt(0);
    } else {
      buffer.writeResourceLocation(BuiltInRegistries.FLUID.getKey(fluid.getFluid()));
      buffer.writeVarInt(fluid.getAmount());
    }
  }

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    HandleClient.handle(this);
  }

  /** Interface to implement for anything wishing to receive fluid updates */
  public interface IFluidPacketReceiver {

    /**
     * Updates the current fluid to the specified value
     *
     * @param fluid New fluidstack
     */
    void updateFluidTo(FluidStack fluid);
  }

  /** Safely runs client side only code in a method only called on client */
  private static class HandleClient {
    private static void handle(FluidUpdatePacket packet) {
      BlockEntityHelper.get(IFluidPacketReceiver.class, Minecraft.getInstance().level, packet.pos).ifPresent(te -> te.updateFluidTo(packet.fluid));
    }
  }
}
