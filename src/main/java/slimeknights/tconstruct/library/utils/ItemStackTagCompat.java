package slimeknights.tconstruct.library.utils;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nullable;

/** Helpers to bridge legacy ItemStack NBT access to 1.21 custom data components. */
public final class ItemStackTagCompat {

  /**
   * Used where no live {@link HolderLookup.Provider} is available (e.g. persisted mod/tool NBT).
   * Prefer passing a server's registry lookup when constructing stacks from datapack/registry content.
   */
  public static final HolderLookup.Provider FALLBACK_REGISTRY = (HolderLookup.Provider) RegistryAccess.EMPTY;

  private ItemStackTagCompat() {}

  public static CompoundTag writeStack(ItemStack stack) {
    CompoundTag compoundTag = new CompoundTag();
    stack.save(FALLBACK_REGISTRY, compoundTag);
    return compoundTag;
  }

  public static ItemStack readStack(CompoundTag compoundTag) {
    return ItemStack.parse(FALLBACK_REGISTRY, compoundTag).orElse(ItemStack.EMPTY);
  }

  /** True when the stack carries {@link DataComponents#CUSTOM_DATA} (closest analogue to pre-1.20.5 {@link ItemStack#hasTag()} for recipe/datagen checks). */
  public static boolean hasCustomData(ItemStack stack) {
    return stack.has(DataComponents.CUSTOM_DATA);
  }

  @Nullable
  public static CompoundTag getTag(ItemStack stack) {
    CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
    return customData != null ? customData.copyTag() : null;
  }

  public static CompoundTag getOrCreateTag(ItemStack stack) {
    CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
    return customData.copyTag();
  }

  public static void setTag(ItemStack stack, CompoundTag tag) {
    if (tag.isEmpty()) {
      stack.remove(DataComponents.CUSTOM_DATA);
    } else {
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
  }
}
