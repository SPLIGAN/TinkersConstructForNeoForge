package slimeknights.tconstruct.gadgets.capability;

import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.WeakHashMap;

/** Capability logic */
public class PiggybackCapability {
  private static final Map<Player, PiggybackHandler> HANDLERS = new WeakHashMap<>();

  private PiggybackCapability() {}

  /** Registers this capability */
  public static void register() {
    // no-op on 1.21: legacy attach-capability flow was removed.
  }

  public static PiggybackHandler get(Player player) {
    synchronized (HANDLERS) {
      return HANDLERS.computeIfAbsent(player, PiggybackHandler::new);
    }
  }
}
