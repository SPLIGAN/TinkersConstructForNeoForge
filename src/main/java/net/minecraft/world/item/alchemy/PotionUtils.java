package net.minecraft.world.item.alchemy;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Temporary 1.21 compatibility shim for legacy PotionUtils usages. */
@SuppressWarnings({"unused", "null"})
public final class PotionUtils {
  public static final String TAG_POTION = "Potion";

  private PotionUtils() {}

  public static Potion getPotion(CompoundTag tag) {
    if (tag != null && tag.contains(TAG_POTION)) {
      ResourceLocation id = ResourceLocation.tryParse(tag.getString(TAG_POTION));
      if (id != null) {
        return BuiltInRegistries.POTION.getOptional(id).orElse(Potions.WATER.value());
      }
    }
    return Potions.WATER.value();
  }

  public static Potion getPotion(ItemStack stack) {
    PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    return contents.potion().map(Holder::value).orElse(Potions.WATER.value());
  }

  public static ItemStack setPotion(ItemStack stack, Holder<Potion> potion) {
    stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
    return stack;
  }

  public static ItemStack setPotion(ItemStack stack, Potion potion) {
    return setPotion(stack, BuiltInRegistries.POTION.wrapAsHolder(potion));
  }

  public static List<MobEffectInstance> getMobEffects(ItemStack stack) {
    return getAllEffects(stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY));
  }

  public static List<MobEffectInstance> getAllEffects(CompoundTag tag) {
    return new ArrayList<>(getPotion(tag).getEffects());
  }

  public static int getColor(ItemStack stack) {
    return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getColor();
  }

  public static int getColor(Iterable<MobEffectInstance> effects) {
    return PotionContents.getColor(effects);
  }

  public static void addPotionTooltip(ItemStack stack, List<Component> tooltip, float durationFactor) {
    PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    contents.addPotionTooltip(tooltip::add, 1.0f, durationFactor);
  }

  private static List<MobEffectInstance> getAllEffects(PotionContents contents) {
    List<MobEffectInstance> effects = new ArrayList<>();
    contents.forEachEffect(effects::add);
    return effects;
  }
}
