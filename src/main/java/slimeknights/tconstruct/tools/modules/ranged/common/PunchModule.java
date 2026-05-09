package slimeknights.tconstruct.tools.modules.ranged.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.json.LevelingValue;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.entity.ProjectileWithKnockback;
import slimeknights.tconstruct.library.modifiers.hook.ranged.ProjectileHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition;
import slimeknights.tconstruct.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;

import javax.annotation.Nullable;
import java.util.List;

/** Module implementing the punch modifier */
public record PunchModule(LevelingValue amount, ModifierCondition<IToolStackView> condition) implements ModifierModule, ProjectileLaunchModifierHook.NoShooter, ProjectileHitModifierHook, ConditionalModule<IToolStackView> {

  /** NBT-backed knockback bonus for arrows (vanilla removed {@link AbstractArrow#setKnockback(int)}). */
  private static final ResourceLocation LAUNCH_KNOCKBACK_TAG = TConstruct.getResource("punch_bonus");

  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<PunchModule>defaultHooks(
    ModifierHooks.PROJECTILE_LAUNCH, ModifierHooks.PROJECTILE_SHOT, ModifierHooks.PROJECTILE_HIT);
  public static final RecordLoadable<PunchModule> LOADER = RecordLoadable.create(LevelingValue.LOADABLE.directField(PunchModule::amount), ModifierCondition.TOOL_FIELD, PunchModule::new);

  @Override
  public RecordLoadable<PunchModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void onProjectileShoot(IToolStackView tool, ModifierEntry modifier, @Nullable LivingEntity shooter, ItemStack ammo, Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData, boolean primary) {
    if (condition.matches(tool, modifier)) {
      float amount = this.amount.compute(modifier.getEffectiveLevel());
      if (amount > 0) {
        if (arrow != null) {
          persistentData.putFloat(LAUNCH_KNOCKBACK_TAG, persistentData.getFloat(LAUNCH_KNOCKBACK_TAG) + amount);
        } else if (projectile instanceof ProjectileWithKnockback withKnockback) {
          withKnockback.addKnockback(amount);
        }
      }
    }
  }

  @Override
  public boolean onProjectileHitEntity(ModifierNBT modifiers, ModDataNBT persistentData, ModifierEntry modifier, Projectile projectile, EntityHitResult hit, @Nullable LivingEntity attacker, @Nullable LivingEntity target, boolean notBlocked) {
    if (!notBlocked || target == null) {
      return false;
    }
    float bonus = persistentData.getFloat(LAUNCH_KNOCKBACK_TAG);
    if (bonus <= 0) {
      return false;
    }
    bonus *= Math.max(0, 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
    Vec3 motion = projectile.getDeltaMovement().multiply(1, 0, 1).normalize().scale(bonus);
    if (motion.lengthSqr() > 0) {
      target.push(motion.x, 0.1f, motion.z);
    }
    return false;
  }
}
