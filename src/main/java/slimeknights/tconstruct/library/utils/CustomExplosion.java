package slimeknights.tconstruct.library.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import slimeknights.tconstruct.library.tools.helper.ToolAttackUtil;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/** Helper class for more control over explosions */
public class CustomExplosion extends Explosion {
  /** Size of the hollowed out cube determining the number of rays to cast */
  private static final int RAY_COUNT = 16;
  private static final int MAX_RAY = RAY_COUNT - 1;
  private static Predicate<Entity> defaultEntityPredicate(CustomExplosion explosion) {
    return entity -> entity != null && entity.isAlive() && !entity.ignoreExplosion(explosion) && !entity.isSpectator();
  }

  /** Maximum damage to deal; setting to 7*2*radius will match the vanilla explosion. */
  protected final float damage;
  /** Entity knockback scale. May be 0 to prevent knockback or negative to reverse knockback */
  protected final float knockback;
  /** Determines which entities are affected by this explosion */
  protected final Predicate<Entity> entityPredicate;
  /** If true, explosion damage bypasses the invulnerability time */
  protected final boolean bypassInvulnerableTime;

  /** Mirrors {@link Explosion} internals (private fields) for subclass logic — see {@link #makeOwnedCalculator(Entity)}. */
  private final Level explosionLevel;
  private final ExplosionDamageCalculator explosionDamageCalculator;
  private final DamageSource explosionDamageSource;
  private final boolean explosionPlacesFire;

  /** For subclasses (e.g. {@code EFLNExplosion}) that need vanilla-style block sampling without accessing private {@link Explosion} fields. */
  protected Level explosionLevel() {
    return explosionLevel;
  }

  protected ExplosionDamageCalculator explosionDamageCalculator() {
    return explosionDamageCalculator;
  }

  protected boolean explosionPlacingFire() {
    return explosionPlacesFire;
  }

  public CustomExplosion(Level level, Vec3 location, float radius, @Nullable Entity sourceEntity, @Nullable Predicate<Entity> entityPredicate, float damage, @Nullable DamageSource damageSource, float knockback, @Nullable ExplosionDamageCalculator damageCalculator, boolean placeFire, BlockInteraction blockInteraction, boolean bypassInvulnerableTime) {
    super(
      level,
      sourceEntity,
      damageSource,
      damageCalculator,
      location.x,
      location.y,
      location.z,
      radius,
      placeFire,
      blockInteraction,
      ParticleTypes.EXPLOSION,
      ParticleTypes.EXPLOSION_EMITTER,
      SoundEvents.GENERIC_EXPLODE
    );
    this.entityPredicate = Objects.requireNonNullElseGet(entityPredicate, () -> defaultEntityPredicate(this));
    this.damage = damage;
    this.knockback = knockback;
    this.bypassInvulnerableTime = bypassInvulnerableTime;
    this.explosionLevel = level;
    this.explosionDamageSource = damageSource != null ? damageSource : level.damageSources().explosion(this);
    this.explosionDamageCalculator = damageCalculator != null ? damageCalculator : makeOwnedCalculator(sourceEntity);
    this.explosionPlacesFire = placeFire;
  }

  public CustomExplosion(Level level, Vec3 location, float radius, @Nullable Entity sourceEntity, @Nullable Predicate<Entity> entityPredicate, float damage, @Nullable DamageSource damageSource, float knockback, @Nullable ExplosionDamageCalculator damageCalculator, boolean placeFire, BlockInteraction blockInteraction) {
    this(level, location, radius, sourceEntity, entityPredicate, damage, damageSource, knockback, damageCalculator, placeFire, blockInteraction, false);
  }

  /** Same default resolution as Explosion#createDamageCalculator — kept here because those fields are not visible to subclasses. */
  private static ExplosionDamageCalculator makeOwnedCalculator(@Nullable Entity sourceEntity) {
    return sourceEntity == null
      ? new ExplosionDamageCalculator()
      : new EntityBasedExplosionDamageCalculator(sourceEntity);
  }

  @Override
  public void explode() {
    explosionLevel.gameEvent(getDirectSourceEntity(), GameEvent.EXPLODE, center());
    calculateHitBlocks();
    damageAndPushEntities();
  }

  /** Calculates the list of blocks to hit; the actual block damage won't happen until {@link #finalizeExplosion(boolean)} */
  protected void calculateHitBlocks() {
    // optimization: if we are not interacting with blocks, no need to calculate blocks
    if (!interactsWithBlocks() && !explosionPlacesFire) {
      return;
    }

    Set<BlockPos> set = new HashSet<>();
    Vec3 ctr = center();
    // loop over a hollowed out 16x cube
    for (int rayX = 0; rayX < RAY_COUNT; rayX++) {
      for (int rayY = 0; rayY < RAY_COUNT; rayY++) {
        for (int rayZ = 0; rayZ < RAY_COUNT; rayZ++) {
          if (rayX == 0 || rayX == MAX_RAY || rayY == 0 || rayY == MAX_RAY || rayZ == 0 || rayZ == MAX_RAY) {
            // determine direction to go, then step in 0.3 unit vector increments
            double stepX = rayX * 2.0 / MAX_RAY - 1;
            double stepY = rayY * 2.0 / MAX_RAY - 1;
            double stepZ = rayZ * 2.0 / MAX_RAY - 1;
            double stepScale = 0.3f / Math.sqrt(stepX * stepX + stepY * stepY + stepZ * stepZ);
            stepX *= stepScale;
            stepY *= stepScale;
            stepZ *= stepScale;

            // keep moving in the direction of the ray until we run out of power; means blocks with high blast resistance shield those with less
            double targetX = ctr.x;
            double targetY = ctr.y;
            double targetZ = ctr.z;
            for (float power = radius() * (0.7f + explosionLevel.random.nextFloat() * 0.6f); power > 0; power -= 0.225f) {
              BlockPos target = BlockPos.containing(targetX, targetY, targetZ);
              BlockState block = explosionLevel.getBlockState(target);
              FluidState fluid = explosionLevel.getFluidState(target);
              if (!explosionLevel.isInWorldBounds(target)) {
                break;
              }

              // reduce power based on blast resistance
              Optional<Float> resistance = explosionDamageCalculator.getBlockExplosionResistance(this, explosionLevel, target, block, fluid);
              if (resistance.isPresent()) {
                power -= (resistance.get() + 0.3f) * 0.3f;
              }

              // remove block if power is high enough
              // optimization: skip air if not placing fires to save network traffic
              if ((explosionPlacesFire || !block.isAir()) && power > 0 && explosionDamageCalculator.shouldBlockExplode(this, explosionLevel, target, block, power)) {
                set.add(target);
              }

              // vanilla difference - we moved the 0.3 multiplier to the original step variables to avoid needing to compute as often
              targetX += stepX;
              targetY += stepY;
              targetZ += stepZ;
            }
          }
        }
      }
    }
    getToBlow().addAll(set);
  }

  /** Called to run the logic for damaging and blasting back entities in range */
  protected void damageAndPushEntities() {
    // skip running if we disabled both damage and knockback as there is nothing left to do
    if (damage <= 0 && knockback == 0) {
      return;
    }

    float diameter = radius() * 2;
    Vec3 ctr = center();
    // small behavior change: we filter the list of entities on fetch, meaning the forge event gets the filtered list
    List<Entity> list = explosionLevel.getEntities(
      getDirectSourceEntity(),
      new AABB(Math.floor(ctr.x - diameter - 1),
               Math.floor(ctr.y - diameter - 1),
               Math.floor(ctr.z - diameter - 1),
               Math.floor(ctr.x + diameter + 1),
               Math.floor(ctr.y + diameter + 1),
               Math.floor(ctr.z + diameter + 1)),
      entityPredicate);
    EventHooks.onExplosionDetonate(explosionLevel, this, list, diameter);

    // start pushing entities
    // this logic is for the most part identical to vanilla, except taking better advantage of vec3
    for (Entity entity : list) {
      Vec3 dir = entity.position().subtract(ctr);
      double length = dir.length();
      double distance = length / diameter;
      if (distance <= 1) {
        // non-TNT uses eye height for explosion direction
        if (!(entity instanceof PrimedTnt)) {
          dir = dir.add(0, entity.getEyeY() - entity.getY(), 0);
          length = dir.length();
        }
        // vanilla change: a bit of tolerance on the length check to match normalize
        if (length > 1.0E-4D) {
          double strength = (1 - distance) * getSeenPercent(ctr, entity);
          // vanilla change: instead of multiplying the damage by 7, we make that a parameter, which can be 0 for no damage
          if (damage > 0) {
            int toDeal = (int) ((strength * strength + strength) / 2 * damage + 1);
            if (bypassInvulnerableTime) {
              ToolAttackUtil.hurtNoInvulnerableTime(entity, explosionDamageSource, toDeal);
            } else {
              entity.hurt(explosionDamageSource, toDeal);
            }
          }

          // apply enchantment to reduce knockback
          if (knockback != 0) {
            double adjustedStrength = strength * knockback;
            Vec3 velocity = dir.scale(adjustedStrength / length);
            velocity = EventHooks.getExplosionKnockback(explosionLevel, this, entity, velocity);
            entity.setDeltaMovement(entity.getDeltaMovement().add(velocity));
            if (entity instanceof Player player) {
              if (!player.isCreative() || !player.getAbilities().flying) {
                getHitPlayers().put(player, velocity);
              }
            }
          }
        }
      }
    }
  }

  /** Runs the logic on the server, syncing to the client. Mirrors {@link ServerLevel#explode}. */
  public void handleServer() {
    // based on ServerLevel#explode
    if (!explosionLevel.isClientSide) {
      if (!EventHooks.onExplosionStart(explosionLevel, this)) {
        explode();
        finalizeExplosion(false);
        syncToClient();
      }
    }
  }

  /** Runs the logic on both sides */
  public void doDualSide(Level level, boolean spawnParticles) {
    if (!EventHooks.onExplosionStart(level, this)) {
      explode();
      finalizeExplosion(spawnParticles);
    }
  }

  /** Syncs this explosion to the client */
  public void syncToClient() {
    if (!explosionLevel.isClientSide && explosionLevel instanceof ServerLevel server) {
      // skip position sync if there are no blocks to be removed
      List<BlockPos> blocks = interactsWithBlocks() ? getToBlow() : List.of();
      Vec3 ctr = center();
      for (ServerPlayer player : server.players()) {
        if (player.distanceToSqr(ctr) < 4096.0D) {
          Vec3 knockback = getHitPlayers().get(player);
          Holder<SoundEvent> sound = getExplosionSound();
          player.connection.send(new ClientboundExplodePacket(
            ctr.x(), ctr.y(), ctr.z(),
            radius(),
            blocks,
            knockback,
            getBlockInteraction(),
            getSmallExplosionParticles(),
            getLargeExplosionParticles(),
            sound
          ));
        }
      }
    }
  }
}
