package slimeknights.tconstruct.library.tools.item.armor;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.tags.EnchantmentTags;
import net.neoforged.neoforge.common.ItemAbility;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.client.TooltipKey;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.behavior.EnchantmentModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.display.DurabilityDisplayModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.SlotStackModifierHook;
import slimeknights.tconstruct.library.tools.IndestructibleItemEntity;
import slimeknights.tconstruct.library.tools.capability.inventory.ToolInventoryCapability;
import slimeknights.tconstruct.library.tools.definition.ModifiableArmorMaterial;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.definition.module.display.ToolNameHook;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.helper.TooltipUtil;
import slimeknights.tconstruct.library.tools.item.IModifiableDisplay;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.library.utils.Util;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class ModifiableArmorItem extends ArmorItem implements IModifiableDisplay {
  private static final ImmutableBiMap<ArmorItem.Type, ResourceLocation> ARMOR_BASE_MODIFIER_IDS = ImmutableBiMap.of(
      Type.HELMET, TConstruct.getResource("armor.base.helmet"),
      Type.CHESTPLATE, TConstruct.getResource("armor.base.chestplate"),
      Type.LEGGINGS, TConstruct.getResource("armor.base.leggings"),
      Type.BOOTS, TConstruct.getResource("armor.base.boots"),
      Type.BODY, TConstruct.getResource("armor.base.body")
  );

  /** Volatile modifier tag to make piglins neutal when worn */
  public static final ResourceLocation PIGLIN_NEUTRAL = TConstruct.getResource("piglin_neutral");
  /** Volatile modifier tag to make this item an elytra */
  public static final ResourceLocation ELYTRA = TConstruct.getResource("elyta");
  /** Volatile flag for a boot item to walk on powdered snow. Cold immunity is handled through a tag */
  public static final ResourceLocation SNOW_BOOTS = TConstruct.getResource("snow_boots");
  /** Volatile flag for an item to act as an enderman mask, stopping them from getting angry. */
  public static final ResourceLocation ENDERMASK = TConstruct.getResource("endermask");

  @Getter
  private final ToolDefinition toolDefinition;
  /** Cache of the tool built for rendering */
  private ItemStack toolForRendering = null;
  public ModifiableArmorItem(ArmorMaterial materialIn, ArmorItem.Type type, Properties builderIn, ToolDefinition toolDefinition) {
    super(Holder.direct(materialIn), type, builderIn);
    this.toolDefinition = toolDefinition;
  }

  public ModifiableArmorItem(ModifiableArmorMaterial material, ArmorItem.Type type, Properties properties) {
    this(material.armorMaterial(), type, properties, Objects.requireNonNull(material.getArmorDefinition(type), "Missing tool definition for " + type.getName()));
  }

  /* Basic properties */

  @Override
  public int getMaxStackSize(ItemStack stack) {
    return 1;
  }

  @Override
  public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
    return ModifierUtil.checkVolatileFlag(stack, PIGLIN_NEUTRAL);
  }

  @Override
  public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
    return type == Type.BOOTS && ModifierUtil.checkVolatileFlag(stack, SNOW_BOOTS);
  }

  @Override
  public boolean isEnderMask(ItemStack stack, Player player, EnderMan endermanEntity) {
    return type == Type.HELMET && ModifierUtil.checkVolatileFlag(stack, ENDERMASK);
  }

  @Override
  public boolean canPerformAction(ItemStack stack, ItemAbility ItemAbility) {
    return ModifierUtil.canPerformAction(ToolStack.from(stack), ItemAbility);
  }

  @Override
  public boolean isNotReplaceableByPickAction(ItemStack stack, Player player, int inventorySlot) {
    return true;
  }


  /* Enchantments */

  @Override
  public boolean isEnchantable(ItemStack stack) {
    return false;
  }

  @Override
  public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
    return false;
  }

  @Override
  public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
    return enchantment.is(EnchantmentTags.CURSE) && super.supportsEnchantment(stack, enchantment);
  }

  @Override
  public int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
    return EnchantmentModifierHook.getEnchantmentLevel(stack, enchantment.value());
  }

  @Override
  public ItemEnchantments getAllEnchantments(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> lookup) {
    return EnchantmentModifierHook.getAllEnchantmentData(stack, lookup);
  }


  /* Loading */

  @Override
  public void onCraftedBy(ItemStack stack, Level levelIn, Player playerIn) {
    ToolStack.ensureInitialized(stack, getToolDefinition());
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level levelIn, Player playerIn, InteractionHand handIn) {
    if (playerIn.isCrouching()) {
      ItemStack stack = playerIn.getItemInHand(handIn);
      InteractionResult result = ToolInventoryCapability.tryOpenContainer(stack, null, getToolDefinition(), playerIn, Util.getSlotType(handIn));
      if (result.consumesAction()) {
        return new InteractionResultHolder<>(result, stack);
      }
    }
    return super.use(levelIn, playerIn, handIn);
  }


  /* Display */

  @Override
  public boolean isFoil(ItemStack stack) {
    // we use enchantments to handle some modifiers, so don't glow from them
    // however, if a modifier wants to glow let them
    return ModifierUtil.checkVolatileFlag(stack, SHINY);
  }

  @Override
  public Component getHighlightTip(ItemStack stack, Component displayName) {
    UnaryOperator<Style> mod = slimeknights.tconstruct.library.modifiers.modules.build.RarityModule.getRarity(stack).getStyleModifier();
    return displayName.copy().withStyle(s -> mod.apply(s));
  }


  /* Indestructible items */

  @Override
  public boolean hasCustomEntity(ItemStack stack) {
    return IndestructibleItemEntity.hasCustomEntity(stack);
  }

  @Nullable
  @Override
  public Entity createEntity(Level level, Entity original, ItemStack stack) {
    return IndestructibleItemEntity.createFrom(level, original, stack);
  }


  /* Damage/Durability */

  @Override
  public boolean isRepairable(ItemStack stack) {
    // handle in the tinker station
    return false;
  }

  @Override
  public int getMaxDamage(ItemStack stack) {
    return ToolDamageUtil.getFakeMaxDamage(stack);
  }

  @Override
  public int getDamage(ItemStack stack) {
    if (!stack.isDamageableItem()) {
      return 0;
    }
    return ToolStack.from(stack).getDamage();
  }

  @Override
  public void setDamage(ItemStack stack, int damage) {
    if (stack.isDamageableItem()) {
      ToolStack.from(stack).setDamage(damage);
    }
  }

  @Override
  public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T damager, Consumer<net.minecraft.world.item.Item> onBroken) {
    // We basically emulate Itemstack.damageItem here. We always return 0 to skip the handling in ItemStack.
    // If we don't tools ignore our damage logic
    if (damager != null && stack.isDamageableItem() && ToolDamageUtil.damage(ToolStack.from(stack), amount, damager, stack)) {
      onBroken.accept(stack.getItem());
    }

    return 0;
  }


  /* Durability display */

  @Override
  public boolean isBarVisible(ItemStack pStack) {
    return DurabilityDisplayModifierHook.showDurabilityBar(pStack);
  }

  @Override
  public int getBarColor(ItemStack pStack) {
    return DurabilityDisplayModifierHook.getDurabilityRGB(pStack);
  }

  @Override
  public int getBarWidth(ItemStack pStack) {
    return DurabilityDisplayModifierHook.getDurabilityWidth(pStack);
  }


  /* Armor properties */

  @Override
  public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
    return false;
  }


  @Override
  public Multimap<Attribute,AttributeModifier> getAttributeModifiers(IToolStackView tool, EquipmentSlot slot) {
    if (slot != getEquipmentSlot()) {
      return ImmutableMultimap.of();
    }

    ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
    if (!tool.isBroken()) {
      // base stats
      StatsNBT statsNBT = tool.getStats();
      ResourceLocation baseId = ARMOR_BASE_MODIFIER_IDS.get(type);
      float armor = statsNBT.get(ToolStats.ARMOR);
      if (armor > 0 && baseId != null) {
        builder.put(Attributes.ARMOR.value(), new AttributeModifier(baseId.withSuffix("/armor"), armor, AttributeModifier.Operation.ADD_VALUE));
      }
      float toughness = statsNBT.get(ToolStats.ARMOR_TOUGHNESS);
      if (toughness > 0 && baseId != null) {
        builder.put(Attributes.ARMOR_TOUGHNESS.value(), new AttributeModifier(baseId.withSuffix("/toughness"), toughness, AttributeModifier.Operation.ADD_VALUE));
      }
      double knockbackResistance = statsNBT.get(ToolStats.KNOCKBACK_RESISTANCE);
      if (knockbackResistance > 0 && baseId != null) {
        builder.put(Attributes.KNOCKBACK_RESISTANCE.value(), new AttributeModifier(baseId.withSuffix("/knockback_resistance"), knockbackResistance, AttributeModifier.Operation.ADD_VALUE));
      }
      // grab attributes from modifiers
      BiConsumer<Attribute,AttributeModifier> attributeConsumer = builder::put;
      for (ModifierEntry entry : tool.getModifierList()) {
        entry.getHook(ModifierHooks.ATTRIBUTES).addAttributes(tool, entry, slot, attributeConsumer);
      }
    }

    return builder.build();
  }

  @Override
  public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
    ToolStack tool = ToolStack.from(stack);
    if (tool.isBroken()) {
      return ItemAttributeModifiers.EMPTY;
    }
    EquipmentSlot slot = getEquipmentSlot();
    Multimap<Attribute, AttributeModifier> mods = getAttributeModifiers(tool, slot);
    ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
    EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(slot);
    for (Map.Entry<Attribute, AttributeModifier> entry : mods.entries()) {
      builder.add(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(entry.getKey()), entry.getValue(), group);
    }
    return builder.build();
  }


  /* Elytra */

  @Override
  public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
    return type == Type.CHESTPLATE && !ToolDamageUtil.isBroken(stack) && ModifierUtil.checkVolatileFlag(stack, ELYTRA);
  }

  @Override
  public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
    if (getEquipmentSlot() == EquipmentSlot.CHEST) {
      ToolStack tool = ToolStack.from(stack);
      if (!tool.isBroken()) {
        // if any modifier says stop flying, stop flying
        for (ModifierEntry entry : tool.getModifierList()) {
          if (entry.getHook(ModifierHooks.ELYTRA_FLIGHT).elytraFlightTick(tool, entry, entity, flightTicks)) {
            return false;
          }
        }
        // damage the tool and keep flying
        if (!entity.level().isClientSide && (flightTicks + 1) % 20 == 0) {
          ToolDamageUtil.damageAnimated(tool, 1, entity, EquipmentSlot.CHEST);
        }
        return true;
      }
    }
    return false;
  }


  /* Ticking */

  @Override
  public void inventoryTick(ItemStack stack, Level levelIn, Entity entityIn, int itemSlot, boolean isSelected) {
    // don't care about non-living, they skip most tool context
    if (entityIn instanceof LivingEntity living) {
      ToolStack tool = ToolStack.from(stack);
      if (!levelIn.isClientSide) {
        tool.ensureHasData();
      }
      List<ModifierEntry> modifiers = tool.getModifierList();
      if (!modifiers.isEmpty()) {
        boolean isCorrectSlot = living.getItemBySlot(getEquipmentSlot()) == stack;
        // we pass in the stack for most custom context, but for the sake of armor its easier to tell them that this is the correct slot for effects
        for (ModifierEntry entry : modifiers) {
          entry.getHook(ModifierHooks.INVENTORY_TICK).onInventoryTick(tool, entry, levelIn, living, itemSlot, isSelected, isCorrectSlot, stack);
        }
      }
    }
  }

  @Override
  public boolean overrideStackedOnOther(ItemStack held, Slot slot, ClickAction action, Player player) {
    return SlotStackModifierHook.overrideStackedOnOther(held, slot, action, player) || super.overrideStackedOnOther(held, slot, action, player);
  }

  @Override
  public boolean overrideOtherStackedOnMe(ItemStack slotStack, ItemStack held, Slot slot, ClickAction action, Player player, SlotAccess access) {
    return SlotStackModifierHook.overrideOtherStackedOnMe(slotStack, held, slot, action, player, access) || super.overrideOtherStackedOnMe(slotStack, held, slot, action, player, access);
  }


  /* Tooltips */

  @Override
  public Component getName(ItemStack stack) {
    return ToolNameHook.getName(getToolDefinition(), stack);
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    TooltipUtil.addInformation(this, stack, context.level(), tooltip, SafeClientAccess.getTooltipKey(), flag);
  }

  @Override
  public List<Component> getStatInformation(IToolStackView tool, @Nullable Player player, List<Component> tooltips, TooltipKey key, TooltipFlag tooltipFlag) {
    tooltips = TooltipUtil.getArmorStats(tool, player, tooltips, key, tooltipFlag);
    TooltipUtil.addAttributes(this, tool, player, tooltips, TooltipUtil.SHOW_ARMOR_ATTRIBUTES, getEquipmentSlot());
    return tooltips;
  }

  /* Display items */

  @Override
  public ItemStack getRenderTool() {
    if (toolForRendering == null) {
      toolForRendering = ToolBuildHandler.buildToolForRendering(this, this.getToolDefinition());
    }
    return toolForRendering;
  }
}
