package slimeknights.tconstruct.tables.network;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.network.packet.IThreadsafePacket;

public class UpdateStationScreenPacket implements IThreadsafePacket {
  public static final UpdateStationScreenPacket INSTANCE = new UpdateStationScreenPacket();

  private UpdateStationScreenPacket() {}

  @Override
  public void encode(FriendlyByteBuf packetBuffer) {}

  @Override
  public void handleThreadsafe(IPayloadContext context) {
    HandleClient.handle();
  }

  /** Safely runs client side only code in a method only called on client */
  private static class HandleClient {
    private static void handle() {
      // Client station screen classes are excluded in server-focused compatibility build.
    }
  }
}
