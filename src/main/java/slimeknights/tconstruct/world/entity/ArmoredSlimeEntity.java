package slimeknights.tconstruct.world.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.List;

public abstract class ArmoredSlimeEntity extends Slime {
  private static final EntityDataAccessor<Boolean> METAL = SynchedEntityData.defineId(ArmoredSlimeEntity.class, EntityDataSerializers.BOOLEAN);
  public static final String TAG_METAL = "metal";
  public ArmoredSlimeEntity(EntityType<? extends ArmoredSlimeEntity> type, Level world) {
    super(type, world);
    if (!world.isClientSide) {
      tryAddAttribute(Attributes.ARMOR, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("tconstruct", "small_armor_bonus"), 3, Operation.ADD_MULTIPLIED_TOTAL));
      tryAddAttribute(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("tconstruct", "small_toughness_bonus"), 3, Operation.ADD_MULTIPLIED_TOTAL));
      tryAddAttribute(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(ResourceLocation.fromNamespaceAndPath("tconstruct", "small_resistence_bonus"), 3, Operation.ADD_MULTIPLIED_TOTAL));
    }
    this.entityData.set(METAL, false);
  }

  @SuppressWarnings("unchecked")
  @Override
  public EntityType<? extends ArmoredSlimeEntity> getType() {
    return (EntityType<? extends ArmoredSlimeEntity>)super.getType();
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder builder) {
    super.defineSynchedData(builder);
    builder.define(METAL, false);
  }

  /** Sets this slime to have a metal core */
  protected void setMetal(boolean metal) {
    this.entityData.set(METAL, metal);
  }

  /** Returns true if the slime has a metal core */
  public boolean isMetal() {
    return this.entityData.get(METAL);
  }

  /** Adds an attribute if possible */
  private void tryAddAttribute(Holder<Attribute> attribute, AttributeModifier modifier) {
    AttributeInstance instance = getAttribute(attribute);
    if (instance != null) {
      instance.addTransientModifier(modifier);
    }
  }

  @Nullable
  @Override
  public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance difficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData) {
    SpawnGroupData spawnData = super.finalizeSpawn(pLevel, difficulty, pReason, pSpawnData);
    this.setCanPickUpLoot(this.random.nextFloat() < (0.55f * difficulty.getSpecialMultiplier()));

    this.populateDefaultEquipmentSlots(random, difficulty);

    // pumpkins on halloween
    if (this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
      LocalDate localdate = LocalDate.now();
      if (localdate.get(ChronoField.MONTH_OF_YEAR) == 10 && localdate.get(ChronoField.DAY_OF_MONTH) == 31 && this.random.nextFloat() < 0.25F) {
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(this.random.nextFloat() < 0.1F ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
        this.armorDropChances[EquipmentSlot.HEAD.getIndex()] = 0.0F;
      }
    }

    return spawnData;
  }

  @Override
  protected abstract void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty);

  protected void populateDefaultEquipmentEnchantments(RandomSource random, DifficultyInstance difficulty) {
    // no-op, unused
  }

  public Iterable<ItemStack> getArmorSlots() {
    return List.of(getItemBySlot(EquipmentSlot.HEAD));
  }

  @Override
  public boolean canHoldItem(ItemStack stack) {
    // only pick up items that go in the head slot, don't have a renderer for other slots
    return getEquipmentSlotForItem(stack) == EquipmentSlot.HEAD;
  }

  protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
    ItemStack stack = this.getItemBySlot(EquipmentSlot.HEAD);
    float slotChance = this.getEquipmentDropChance(EquipmentSlot.HEAD);
    // items do not always drop if a large slime, increases chance of inheritance
    // small slimes always drop, no losing gear
    if (slotChance > 0.25f && getSize() > 1) {
      slotChance = 0.25f;
    }
    boolean alwaysDrop = slotChance > 1.0F;
    if (!stack.isEmpty() && (recentlyHit || alwaysDrop)) {
      if ((this.random.nextFloat() - (looting * 0.01f)) < slotChance) {
        if (!alwaysDrop && stack.isDamageableItem()) {
          int max = stack.getMaxDamage();
          stack.setDamageValue(max - this.random.nextInt(1 + this.random.nextInt(Math.max(max - 3, 1))));
        }
        this.spawnAtLocation(stack);
        this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
      }
    }
  }

  @SuppressWarnings("IntegerDivisionInFloatingPointContext")
  @Override
  public void remove(Entity.RemovalReason reason) {
    super.remove(reason);
  }

  @Override
  public void addAdditionalSaveData(CompoundTag tag) {
    super.addAdditionalSaveData(tag);
    tag.putBoolean(TAG_METAL, this.isMetal());
  }

  @Override
  public void readAdditionalSaveData(CompoundTag tag) {
    super.readAdditionalSaveData(tag);
    this.setMetal(tag.getBoolean(TAG_METAL));
  }
}
