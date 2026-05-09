package net.neoforged.neoforge.capabilities;

/**
 * Minimal compatibility wrapper for legacy NeoForge/Forge capability signatures.
 */
public class Capability<T> {
  private final String name;

  public Capability(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "Capability[" + name + "]";
  }
}
