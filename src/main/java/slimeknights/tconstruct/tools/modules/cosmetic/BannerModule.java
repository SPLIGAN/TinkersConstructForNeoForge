package slimeknights.tconstruct.tools.modules.cosmetic;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.data.loadable.record.SingletonLoader;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.hook.display.DisplayNameModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.TooltipModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.utils.TinkerTooltipFlags;
import slimeknights.tconstruct.library.utils.Util;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

/** Module for banner pattern tooltips */
public enum BannerModule implements ModifierModule, DisplayNameModifierHook, TooltipModifierHook {
  INSTANCE;

  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<BannerModule>defaultHooks(ModifierHooks.DISPLAY_NAME, ModifierHooks.TOOLTIP);
  public static final RecordLoadable<BannerModule> LOADER = new SingletonLoader<>(INSTANCE);
  /** Key for a dye color, stored as its ID */
  public static final String KEY_DYE = "dye";
  /** Key for a pattern color, as a 24 bit integer */
  public static final String KEY_COLOR = "color";
  /** Key for a pattern id (namespaced id) or legacy short banner code */
  public static final String KEY_PATTERN = "pattern";
  /** Tooltip key saying hold shift for patterns */
  private static final Component HOLD_SHIFT = TConstruct.makeTranslation("modifier", "banner.hold_shift").withStyle(ChatFormatting.GRAY);

  /** Legacy Java Edition banner NBT “Pattern” short codes -> {@code minecraft:...} pattern id. */
  private static final Map<String, ResourceLocation> LEGACY_BANNER_PATTERN_CODES = new HashMap<>();

  static {
    putLegacy("b", "base");
    putLegacy("bl", "square_bottom_left");
    putLegacy("bo", "border");
    putLegacy("bri", "bricks");
    putLegacy("br", "square_bottom_right");
    putLegacy("bs", "stripe_bottom");
    putLegacy("bt", "triangle_bottom");
    putLegacy("bts", "triangles_bottom");
    putLegacy("cbo", "curly_border");
    putLegacy("cr", "cross");
    putLegacy("cre", "creeper");
    putLegacy("cs", "stripe_center");
    putLegacy("dls", "stripe_downleft");
    putLegacy("drs", "stripe_downright");
    putLegacy("flo", "flower");
    putLegacy("glb", "globe");
    putLegacy("gra", "gradient");
    putLegacy("gru", "gradient_up");
    putLegacy("hh", "half_horizontal");
    putLegacy("hhb", "half_horizontal_bottom");
    putLegacy("ld", "diagonal_left");
    putLegacy("ls", "stripe_left");
    putLegacy("lud", "diagonal_up_left");
    putLegacy("mc", "circle");
    putLegacy("moj", "mojang");
    putLegacy("mr", "rhombus");
    putLegacy("ms", "stripe_middle");
    putLegacy("pig", "piglin");
    putLegacy("rd", "diagonal_right");
    putLegacy("rs", "stripe_right");
    putLegacy("rud", "diagonal_up_right");
    putLegacy("sc", "straight_cross");
    putLegacy("sku", "skull");
    putLegacy("ss", "small_stripes");
    putLegacy("tl", "square_top_left");
    putLegacy("tr", "square_top_right");
    putLegacy("ts", "stripe_top");
    putLegacy("tt", "triangle_top");
    putLegacy("tts", "triangles_top");
  }

  private static void putLegacy(String code, String path) {
    LEGACY_BANNER_PATTERN_CODES.put(code, fromNamespaceAndPath("minecraft", path));
  }

  /** Resolves a banner pattern from stored tooltip/model data into a registry holder (vanilla ids or legacy short codes). */
  public static Optional<Holder<BannerPattern>> resolveBannerPattern(@Nullable RegistryAccess access, String raw) {
    if (access == null || raw.isEmpty()) {
      return Optional.empty();
    }
    ResourceLocation parsed = ResourceLocation.tryParse(raw);
    if (parsed == null) {
      parsed = ResourceLocation.tryParse("minecraft:" + raw);
    }
    if (parsed == null) {
      parsed = LEGACY_BANNER_PATTERN_CODES.get(raw);
    }
    if (parsed == null) {
      return Optional.empty();
    }
    ResourceKey<BannerPattern> key = ResourceKey.create(Registries.BANNER_PATTERN, parsed);
    return access.lookupOrThrow(Registries.BANNER_PATTERN).get(key).map(ref -> (Holder<BannerPattern>) ref);
  }

  @Override
  public RecordLoadable<? extends ModifierModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public Component getDisplayName(IToolStackView tool, ModifierEntry entry, Component name, @Nullable RegistryAccess access) {
    // color the tooltip the color of the first pattern
    ListTag patterns = tool.getPersistentData().getList(patternKey(entry.getId()), ListTag.TAG_COMPOUND);
    if (!patterns.isEmpty()) {
      return name.copy().withStyle(name.getStyle().withColor(DyeColor.byId(patterns.getCompound(0).getInt(KEY_DYE)).getTextColor()));
    }
    return name;
  }

  @Override
  public void addTooltip(IToolStackView tool, ModifierEntry modifier, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
    // add all patterns in a tinker station when holding
    if (tooltipFlag == TinkerTooltipFlags.TINKER_STATION) {
      if (tooltipKey == TooltipKey.SHIFT) {
        RegistryAccess access = player != null ? player.level().registryAccess() : null;
        ListTag patterns = tool.getPersistentData().getList(patternKey(modifier.getId()), ListTag.TAG_COMPOUND);
        for (int i = 0; i < patterns.size(); i++) {
          CompoundTag tag = patterns.getCompound(i);
          DyeColor dye = DyeColor.byId(tag.getInt(KEY_DYE));
          resolveBannerPattern(access, tag.getString(KEY_PATTERN)).ifPresent(holder ->
            holder.unwrapKey().ifPresent(key ->
              tooltip.add(Component.translatable("block.minecraft.banner." + key.location().toShortLanguageKey() + '.' + dye.getName()).withStyle(ChatFormatting.GRAY))));
        }
      } else {
        tooltip.add(HOLD_SHIFT);
      }
    }
  }

  /** Gets the key for the cache used in the model */
  public static ResourceLocation cacheKey(ModifierId modifier) {
    return modifier.withSuffix("_cache");
  }

  /** Gets the key for the pattern list in NBT */
  public static ResourceLocation patternKey(ModifierId modifier) {
    return modifier.withSuffix("_patterns");
  }

  /** Copies the given list of patterns from banner format to the tool's NBT */
  public static void copyPatterns(ModDataNBT data, ModifierId id, DyeColor dye, ListTag banner) {
    int baseColor = Util.getColor(dye);
    ListTag patterns = new ListTag();

    // add in the base pattern, it only exists on shields and we copy from banners
    CompoundTag basePattern = new CompoundTag();
    basePattern.putString(KEY_PATTERN, BannerPatterns.BASE.location().toString());
    basePattern.putInt(KEY_DYE, dye.getId());
    basePattern.putInt(KEY_COLOR, baseColor);
    patterns.add(basePattern);

    // need a cache key, but it's just going to get hashed anyway, so store its hash
    int hashCode = baseColor;

    // add in all other patterns
    for (int i = 0; i < banner.size(); i++) {
      CompoundTag original = banner.getCompound(i);
      CompoundTag copy = new CompoundTag();
      // copy the pattern as is
      String pattern = original.getString("Pattern");
      copy.putString(KEY_PATTERN, pattern);
      // convert the color from a dye color to an integer
      dye = DyeColor.byId(original.getInt("Color"));
      int color = Util.getColor(dye);
      copy.putInt(KEY_DYE, dye.getId()); // dye for the tooltip
      copy.putInt(KEY_COLOR, color); // color for the model
      // add the values
      patterns.add(copy);
      // update the hash code with the new information
      hashCode = 31 * (31 * hashCode + color) + pattern.hashCode();
    }

    // add to tool NBT
    data.put(patternKey(id), patterns);
    data.putInt(cacheKey(id), hashCode);
  }
}
