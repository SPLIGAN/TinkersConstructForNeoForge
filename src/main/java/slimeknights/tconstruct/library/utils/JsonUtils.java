package slimeknights.tconstruct.library.utils;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import slimeknights.mantle.network.packet.ISimplePacket;
import slimeknights.tconstruct.common.network.TinkerNetwork;

/** Helpers for a few JSON related tasks */
public class JsonUtils {
  private JsonUtils() {}

  /** Called when the player logs in to send packets */
  public static void syncPackets(OnDatapackSyncEvent event, ISimplePacket... packets) {
    for (ServerPlayer player : event.getRelevantPlayers().toList()) {
      for (ISimplePacket packet : packets) {
        TinkerNetwork.getInstance().sendTo(packet, player);
      }
    }
  }

  /** Creates a JSON object with the given key set to a resource location */
  public static JsonObject withLocation(String key, ResourceLocation value) {
    JsonObject json = new JsonObject();
    json.addProperty(key, value.toString());
    return json;
  }

  /** Creates a JSON object with the given type set, makes using {@link slimeknights.mantle.data.gson.GenericRegisteredSerializer} easier */
  public static JsonObject withType(ResourceLocation type) {
    return withLocation("type", type);
  }
}
