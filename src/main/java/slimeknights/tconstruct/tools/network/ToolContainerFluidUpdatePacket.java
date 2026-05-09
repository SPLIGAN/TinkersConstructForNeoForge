package slimeknights.tconstruct.tools.network;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.tools.menu.ToolContainerMenu;

/** Packet used when a fluid is changed inside a tool container menu */
public record ToolContainerFluidUpdatePacket(FluidStack fluid) implements IThreadsafePacket {
  public ToolContainerFluidUpdatePacket(FriendlyByteBuf buffer) {
    this(readFluid(buffer));
  }

  private static FluidStack readFluid(FriendlyByteBuf buffer) {
    ResourceLocation fluidId = buffer.readResourceLocation();
    int amount = buffer.readVarInt();
    if (amount <= 0) {
      return FluidStack.EMPTY;
    }
    return new FluidStack(BuiltInRegistries.FLUID.get(fluidId), amount);
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
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
    Player player = SafeClientAccess.getPlayer();
    if (player != null && player.containerMenu instanceof ToolContainerMenu toolMenu) {
      toolMenu.getTank().setFluid(fluid);
    }
  }
}
