package slimeknights.tconstruct.tools.modules.ranged.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.json.LevelingInt;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import javax.annotation.Nullable;
import java.util.List;

/** Module implementing the arrow pierce modifier */
public record ArrowPierceModule(LevelingInt amount, ModifierCondition<IToolStackView> condition) implements ModifierModule, ProjectileLaunchModifierHook.NoShooter, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ArrowPierceModule>defaultHooks(ModifierHooks.PROJECTILE_LAUNCH, ModifierHooks.PROJECTILE_SHOT);

  /** Vanilla made {@code AbstractArrow#setPierceLevel} private; apply extra pierce via the same NBT path the entity uses. */
  private static void applyExtraPierce(AbstractArrow arrow, int extra) {
    if (extra <= 0) {
      return;
    }
    CompoundTag tag = new CompoundTag();
    arrow.addAdditionalSaveData(tag);
    int total = Math.min(127, (tag.getByte("PierceLevel") & 0xFF) + extra);
    tag.putByte("PierceLevel", (byte) total);
    arrow.readAdditionalSaveData(tag);
  }
  public static final RecordLoadable<ArrowPierceModule> LOADER = RecordLoadable.create(LevelingInt.LOADABLE.directField(ArrowPierceModule::amount), ModifierCondition.TOOL_FIELD, ArrowPierceModule::new);

  @Override
  public RecordLoadable<ArrowPierceModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void onProjectileShoot(IToolStackView tool, ModifierEntry modifier, @Nullable LivingEntity shooter, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
    if (condition.matches(tool, modifier) && arrow != null) {
      int amount = this.amount.compute(modifier.getEffectiveLevel());
      applyExtraPierce(arrow, amount);
    }
  }
}
