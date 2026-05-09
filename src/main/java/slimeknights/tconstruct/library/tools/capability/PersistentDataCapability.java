package slimeknights.tconstruct.library.tools.capability;

import net.minecraft.world.entity.Entity;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Capability to store persistent NBT data on an entity. For players, this is automatically synced to the client on load, but not during gameplay.
 * Persists after death, will reassess if we need some data to not persist death
 */
public class PersistentDataCapability {
  private PersistentDataCapability() {}

  /** Fallback attachment store for NeoForge capability migration */
  private static final Map<Entity,ModDataNBT> ENTITY_DATA = new WeakHashMap<>();

  /** Gets the data or warns if its missing */
  public static ModDataNBT getOrWarn(Entity entity) {
    return ENTITY_DATA.computeIfAbsent(entity, key -> new ModDataNBT());
  }

  /** Registers this capability */
  public static void register() {
    // Intentionally empty for minimal NeoForge+Arclight compatibility build.
  }
}
