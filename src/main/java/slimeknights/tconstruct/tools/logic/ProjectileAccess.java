package slimeknights.tconstruct.tools.logic;

import net.minecraft.world.entity.projectile.Projectile;

import java.lang.reflect.Field;

/** Reflective access to {@link Projectile#leftOwner}; package-private in vanilla. */
public final class ProjectileAccess {
  private static final Field LEFT_OWNER;

  static {
    try {
      LEFT_OWNER = Projectile.class.getDeclaredField("leftOwner");
      LEFT_OWNER.setAccessible(true);
    } catch (ReflectiveOperationException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private ProjectileAccess() {}

  public static void setLeftOwner(Projectile projectile, boolean leftOwner) {
    try {
      LEFT_OWNER.setBoolean(projectile, leftOwner);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }
}
