package slimeknights.tconstruct.fluids.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import slimeknights.tconstruct.library.utils.Util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/** Implements filling a bucket with an NBT fluid */
public class PotionBucketItem extends PotionItem {
  private final Supplier<? extends Fluid> supplier;
  public PotionBucketItem(Supplier<? extends Fluid> supplier, Properties builder) {
    super(builder);
    this.supplier = supplier;
  }

  public Fluid getFluid() {
    return supplier.get();
  }

  @Override
  public String getDescriptionId(ItemStack stack) {
    Potion potion = PotionUtils.getPotion(stack);
    String bucketKey = Potion.getName(java.util.Optional.of(net.minecraft.core.registries.BuiltInRegistries.POTION.wrapAsHolder(potion)), getDescriptionId() + ".effect.");
    if (Util.canTranslate(bucketKey)) {
      return bucketKey;
    }
    return super.getDescriptionId();
  }

  @Override
  public Component getName(ItemStack stack) {
    Potion potion = PotionUtils.getPotion(stack);
    String bucketKey = Potion.getName(java.util.Optional.of(net.minecraft.core.registries.BuiltInRegistries.POTION.wrapAsHolder(potion)), getDescriptionId() + ".effect.");
    if (Util.canTranslate(bucketKey)) {
      return Component.translatable(bucketKey);
    }
    // default to filling with the contents
    return Component.translatable(getDescriptionId() + ".contents", Component.translatable(Potion.getName(java.util.Optional.of(net.minecraft.core.registries.BuiltInRegistries.POTION.wrapAsHolder(potion)), "item.minecraft.potion.effect.")));
  }

  @Override
  public ItemStack getDefaultInstance() {
    return PotionUtils.setPotion(new ItemStack(this), Potions.AWKWARD);
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
    Player player = living instanceof Player p ? p : null;
    if (player instanceof ServerPlayer serverPlayer) {
      CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
    }

    // effects are 2x duration
    if (!level.isClientSide) {
      for (MobEffectInstance effect : PotionUtils.getMobEffects(stack)) {
        if (effect.getEffect().value().isInstantenous()) {
          effect.getEffect().value().applyInstantenousEffect(player, player, living, effect.getAmplifier(), 2.5D);
        } else {
          MobEffectInstance newEffect = new MobEffectInstance(effect.getEffect(), effect.getDuration() * 5 / 2, effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
          living.addEffect(newEffect);
        }
      }
    }

    if (player != null) {
      player.awardStat(Stats.ITEM_USED.get(this));
      if (!player.getAbilities().instabuild) {
        stack.shrink(1);
      }
    }

    if (player == null || !player.getAbilities().instabuild) {
      if (stack.isEmpty()) {
        return new ItemStack(Items.BUCKET);
      }
      if (player != null) {
        player.getInventory().add(new ItemStack(Items.BUCKET));
      }
    }
    living.gameEvent(GameEvent.DRINK);
    return stack;
  }

  @Override
  public void appendHoverText(ItemStack pStack, Item.TooltipContext context, List<Component> pTooltip, TooltipFlag pFlag) {
    PotionContents potionContents = pStack.get(DataComponents.POTION_CONTENTS);
    if (potionContents != null) {
      potionContents.addPotionTooltip(pTooltip::add, 2.5f, context.tickRate());
    }
  }

  public int getUseDuration(ItemStack pStack) {
    return 96; // 3x duration of potion bottles
  }

  @SuppressWarnings("rawtypes")
  public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
    return (ICapabilityProvider) new PotionBucketWrapper(stack);
  }

  public static class PotionBucketWrapper extends FluidBucketWrapper {
    public PotionBucketWrapper(ItemStack container) {
      super(container);
    }

    @Nonnull
    @Override
    public FluidStack getFluid() {
      FluidStack fluid = new FluidStack(((PotionBucketItem) container.getItem()).getFluid(), FluidType.BUCKET_VOLUME);
      PotionContents contents = container.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
      fluid.set(DataComponents.POTION_CONTENTS, contents);
      return fluid;
    }
  }
}
