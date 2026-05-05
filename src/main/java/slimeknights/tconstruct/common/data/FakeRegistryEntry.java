package slimeknights.tconstruct.common.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import slimeknights.tconstruct.common.TinkerEffect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Handles creating fake registry entries to datagen entries based on other mods */
public class FakeRegistryEntry {
  private static final Map<ResourceLocation, Block> FAKE_BLOCKS = new ConcurrentHashMap<>();
  private static final Map<ResourceLocation, Item> FAKE_ITEMS = new ConcurrentHashMap<>();
  private static final Map<ResourceLocation, MobEffect> FAKE_EFFECTS = new ConcurrentHashMap<>();
  private static final Map<ResourceLocation, EntityType<?>> FAKE_ENTITIES = new ConcurrentHashMap<>();

  /** Gets or creates a fake block with the given ID */
  public static Block block(ResourceLocation id) {
    return BuiltInRegistries.BLOCK.getOptional(id)
      .orElseGet(() -> FAKE_BLOCKS.computeIfAbsent(id, key -> new Block(BlockBehaviour.Properties.of())));
  }

  /** Gets or creates a fake item with the given ID */
  public static Item item(ResourceLocation id) {
    return BuiltInRegistries.ITEM.getOptional(id)
      .orElseGet(() -> FAKE_ITEMS.computeIfAbsent(id, key -> new Item(new Item.Properties())));
  }

  /** Gets or creates a fake mob effect with the given ID */
  public static MobEffect effect(ResourceLocation id) {
    return BuiltInRegistries.MOB_EFFECT.getOptional(id)
      .orElseGet(() -> FAKE_EFFECTS.computeIfAbsent(id, key -> new TinkerEffect(MobEffectCategory.NEUTRAL, false)));
  }

  /** Gets or creates a fake entity with the given ID */
  public static <T extends Entity> EntityType<?> entity(ResourceLocation id) {
    return BuiltInRegistries.ENTITY_TYPE.getOptional(id)
      .orElseGet(() -> FAKE_ENTITIES.computeIfAbsent(id, key ->
        EntityType.Builder.of((type, level) -> {
          throw new UnsupportedOperationException("Cannot create instance of fake entity");
        }, MobCategory.MISC).build(key.toString())));
  }
}
