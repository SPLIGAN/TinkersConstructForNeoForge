package slimeknights.tconstruct.tools.logic;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Reflective access to {@link AbstractArrow} internals used from outside the class hierarchy.
 * Vanilla uses private/protected members here; access transformers are not always visible to IDEs/javac.
 */
public final class AbstractArrowPiercingAccess {
  private static final Field PIERCING_IGNORE_ENTITY_IDS;
  private static final Field PIERCED_AND_KILLED_ENTITIES;
  private static final Field SOUND_EVENT;
  private static final Method DO_POST_HURT_EFFECTS;
  private static final Method GET_PICKUP_ITEM;

  static {
    try {
      PIERCING_IGNORE_ENTITY_IDS = AbstractArrow.class.getDeclaredField("piercingIgnoreEntityIds");
      PIERCING_IGNORE_ENTITY_IDS.setAccessible(true);
      PIERCED_AND_KILLED_ENTITIES = AbstractArrow.class.getDeclaredField("piercedAndKilledEntities");
      PIERCED_AND_KILLED_ENTITIES.setAccessible(true);
      SOUND_EVENT = AbstractArrow.class.getDeclaredField("soundEvent");
      SOUND_EVENT.setAccessible(true);
      DO_POST_HURT_EFFECTS = AbstractArrow.class.getDeclaredMethod("doPostHurtEffects", LivingEntity.class);
      DO_POST_HURT_EFFECTS.setAccessible(true);
      GET_PICKUP_ITEM = AbstractArrow.class.getDeclaredMethod("getPickupItem");
      GET_PICKUP_ITEM.setAccessible(true);
    } catch (ReflectiveOperationException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private AbstractArrowPiercingAccess() {}

  @Nullable
  public static IntOpenHashSet getPiercingIgnoreEntityIds(AbstractArrow arrow) {
    try {
      return (IntOpenHashSet) PIERCING_IGNORE_ENTITY_IDS.get(arrow);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  public static void setPiercingIgnoreEntityIds(AbstractArrow arrow, @Nullable IntOpenHashSet ids) {
    try {
      PIERCING_IGNORE_ENTITY_IDS.set(arrow, ids);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  @SuppressWarnings("unchecked")
  @Nullable
  public static List<Entity> getPiercedAndKilledEntities(AbstractArrow arrow) {
    try {
      return (List<Entity>) PIERCED_AND_KILLED_ENTITIES.get(arrow);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  public static void setPiercedAndKilledEntities(AbstractArrow arrow, @Nullable List<Entity> entities) {
    try {
      PIERCED_AND_KILLED_ENTITIES.set(arrow, entities);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  public static void doPostHurtEffects(AbstractArrow arrow, LivingEntity target) {
    try {
      DO_POST_HURT_EFFECTS.invoke(arrow, target);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
  }

  public static SoundEvent getSoundEvent(AbstractArrow arrow) {
    try {
      return (SoundEvent) SOUND_EVENT.get(arrow);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  public static ItemStack getPickupItem(AbstractArrow arrow) {
    try {
      return (ItemStack) GET_PICKUP_ITEM.invoke(arrow);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
  }
}
