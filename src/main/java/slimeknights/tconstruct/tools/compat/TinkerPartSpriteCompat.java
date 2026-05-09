package slimeknights.tconstruct.tools.compat;

import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;

/** Minimal constants for server-side compile when datagen providers are excluded. */
public final class TinkerPartSpriteCompat {
  private TinkerPartSpriteCompat() {}

  public static final MaterialStatsId ARMOR_MAILLE = new MaterialStatsId(ResourceLocation.parse("tconstruct:armor_maille"));
  public static final MaterialStatsId ARMOR_CUIRASS = new MaterialStatsId(ResourceLocation.parse("tconstruct:armor_cuirass"));
  public static final MaterialStatsId ARMOR_PLATING = new MaterialStatsId(ResourceLocation.parse("tconstruct:armor_plating"));
  public static final MaterialStatsId SLIMESUIT = new MaterialStatsId(ResourceLocation.parse("tconstruct:slimesuit"));
}
