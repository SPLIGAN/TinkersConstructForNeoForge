package slimeknights.tconstruct.library.utils;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

/**
 * Typed wrapper around {@link ResourceLocation} (vanilla {@code ResourceLocation} is {@code final} on modern versions).
 *
 * @see IdParser
 */
public abstract class ResourceId {
  protected final ResourceLocation location;

  protected ResourceId(ResourceLocation location) {
    this.location = location;
  }

  protected ResourceId(String namespace, String path) {
    this.location = ResourceLocation.fromNamespaceAndPath(namespace, path);
  }

  protected ResourceId(String string) {
    this.location = ResourceLocation.parse(string);
  }

  public ResourceLocation getLocation() {
    return location;
  }

  public String getNamespace() {
    return location.getNamespace();
  }

  public String getPath() {
    return location.getPath();
  }

  /** Appends to the path segment (NeoForge-like {@code ResourceLocation#withSuffix} behavior). */
  public ResourceLocation withSuffix(String suffix) {
    return ResourceLocation.fromNamespaceAndPath(getNamespace(), getPath() + suffix);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o instanceof ResourceLocation rl) {
      return location.equals(rl);
    }
    if (o instanceof ResourceId rid) {
      return location.equals(rid.location);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return location.hashCode();
  }

  @Override
  public String toString() {
    return location.toString();
  }

  protected static String[] decompose(String location, char separator) {
    String namespace = ResourceLocation.DEFAULT_NAMESPACE;
    String path = location;
    int idx = location.indexOf(separator);
    if (idx >= 0) {
      namespace = location.substring(0, idx);
      path = location.substring(idx + 1);
    }
    return new String[]{namespace, path};
  }

  /**
   * Creates a new ID from the given string
   * @param string  String
   * @return  ID, or null if invalid
   */
  @Nullable
  protected static <T extends ResourceId> T tryParse(String string, BiFunction<String, String, T> constructor) {
    String[] parts = decompose(string, ':');
    return tryBuild(parts[0], parts[1], constructor);
  }

  /**
   * Creates a new ID from the given namespace and path
   * @param namespace  Namespace
   * @param path       Path
   * @return  ID, or null if invalid
   */
  @Nullable
  protected static <T extends ResourceId> T tryBuild(String namespace, String path, BiFunction<String, String, T> constructor) {
    if (ResourceLocation.isValidNamespace(namespace) && ResourceLocation.isValidPath(path)) {
      return constructor.apply(namespace, path);
    }
    return null;
  }
}
