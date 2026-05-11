package slimeknights.tconstruct.shared.inventory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import slimeknights.tconstruct.TConstruct;

import javax.annotation.Nullable;
import java.util.Optional;

/** Criteria that triggers when a container is opened */
public class BlockContainerOpenedTrigger extends SimpleCriterionTrigger<BlockContainerOpenedTrigger.Instance> {

  public static BlockContainerOpenedTrigger TRIGGER;

  /** Register with {@link BuiltInRegistries#TRIGGER_TYPES} during common setup */
  public static void bootstrap() {
    if (TRIGGER == null) {
      TRIGGER = Registry.register(
          BuiltInRegistries.TRIGGER_TYPES,
          TConstruct.getResource("block_container_opened"),
          new BlockContainerOpenedTrigger());
    }
  }

  private BlockContainerOpenedTrigger() {}

  @Override
  public Codec<Instance> codec() {
    return Instance.CODEC;
  }

  /** Triggers this criteria */
  public void trigger(@Nullable BlockEntity tileEntity, @Nullable Inventory inv) {
    if (tileEntity != null && inv != null && inv.player instanceof ServerPlayer player) {
      this.trigger(player, instance -> instance.matches(tileEntity.getType()));
    }
  }

  public record Instance(Optional<ContextAwarePredicate> player, ResourceKey<BlockEntityType<?>> blockEntityType)
      implements SimpleCriterionTrigger.SimpleInstance {

    public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            ResourceKey.codec(Registries.BLOCK_ENTITY_TYPE).fieldOf("type").forGetter(Instance::blockEntityType))
        .apply(inst, Instance::new));

    public static Criterion<Instance> container(BlockEntityType<?> type) {
      ResourceKey<BlockEntityType<?>> key = BuiltInRegistries.BLOCK_ENTITY_TYPE.getResourceKey(type).orElseThrow();
      return TRIGGER.createCriterion(new Instance(Optional.empty(), key));
    }

    public boolean matches(BlockEntityType<?> type) {
      return BuiltInRegistries.BLOCK_ENTITY_TYPE.getResourceKey(type).filter(k -> k.equals(blockEntityType)).isPresent();
    }
  }
}
