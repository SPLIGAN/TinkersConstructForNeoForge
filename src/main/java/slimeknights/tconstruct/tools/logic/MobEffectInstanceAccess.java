package slimeknights.tconstruct.tools.logic;

import net.minecraft.world.effect.MobEffectInstance;

import java.lang.reflect.Field;

/** Reflective write to {@link MobEffectInstance#duration}; field is not visible to mods without transformed sources. */
public final class MobEffectInstanceAccess {
  private static final Field DURATION;

  static {
    try {
      DURATION = MobEffectInstance.class.getDeclaredField("duration");
      DURATION.setAccessible(true);
    } catch (ReflectiveOperationException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private MobEffectInstanceAccess() {}

  public static void setDuration(MobEffectInstance instance, int duration) {
    try {
      DURATION.setInt(instance, duration);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }
}
