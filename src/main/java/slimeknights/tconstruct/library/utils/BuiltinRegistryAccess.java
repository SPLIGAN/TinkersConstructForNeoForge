package slimeknights.tconstruct.library.utils;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

/** Access to Minecraft static registries that are no longer fields on {@link BuiltInRegistries}. */
public final class BuiltinRegistryAccess {
  private BuiltinRegistryAccess() {}

  /** @throws ClassCastException if the registry entry is absent or mismatched */
  @SuppressWarnings("unchecked")
  public static <T> Registry<T> get(ResourceKey<? extends Registry<T>> key) {
    return (Registry<T>) BuiltInRegistries.REGISTRY.get(key.location());
  }
}
