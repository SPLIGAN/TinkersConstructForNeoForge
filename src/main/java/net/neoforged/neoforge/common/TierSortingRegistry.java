package net.neoforged.neoforge.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/** Temporary 1.21 compatibility shim for legacy TierSortingRegistry usage. */
@SuppressWarnings("unused")
public final class TierSortingRegistry {
  private TierSortingRegistry() {}

  public static Tier byName(ResourceLocation id) {
    String key = id.toString();
    return switch (key) {
      case "minecraft:wood", "minecraft:wooden" -> Tiers.WOOD;
      case "minecraft:stone" -> Tiers.STONE;
      case "minecraft:iron" -> Tiers.IRON;
      case "minecraft:gold", "minecraft:golden" -> Tiers.GOLD;
      case "minecraft:diamond" -> Tiers.DIAMOND;
      case "minecraft:netherite" -> Tiers.NETHERITE;
      default -> null;
    };
  }

  public static ResourceLocation getName(Tier tier) {
    if (tier == Tiers.WOOD) return ResourceLocation.parse("minecraft:wood");
    if (tier == Tiers.STONE) return ResourceLocation.parse("minecraft:stone");
    if (tier == Tiers.IRON) return ResourceLocation.parse("minecraft:iron");
    if (tier == Tiers.GOLD) return ResourceLocation.parse("minecraft:gold");
    if (tier == Tiers.DIAMOND) return ResourceLocation.parse("minecraft:diamond");
    if (tier == Tiers.NETHERITE) return ResourceLocation.parse("minecraft:netherite");
    return null;
  }

  public static List<Tier> getSortedTiers() {
    return List.of(Tiers.WOOD, Tiers.GOLD, Tiers.STONE, Tiers.IRON, Tiers.DIAMOND, Tiers.NETHERITE);
  }

  public static boolean isCorrectTierForDrops(Tier tier, BlockState state) {
    if (!state.requiresCorrectToolForDrops()) {
      return true;
    }
    int level = tier.getLevel();
    if (state.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
      return level >= Tiers.DIAMOND.getLevel();
    }
    if (state.is(BlockTags.NEEDS_IRON_TOOL)) {
      return level >= Tiers.IRON.getLevel();
    }
    if (state.is(BlockTags.NEEDS_STONE_TOOL)) {
      return level >= Tiers.STONE.getLevel();
    }
    return level >= Tiers.WOOD.getLevel();
  }
}
