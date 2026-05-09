package slimeknights.tconstruct.library.tools.item.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tools.definition.ModifiableArmorMaterial;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;
import slimeknights.tconstruct.library.tools.helper.ArmorUtil;

import javax.annotation.Nullable;

/** Armor model that applies multiple texture layers in order */
public class MultilayerArmorItem extends ModifiableArmorItem {
  public MultilayerArmorItem(ModifiableArmorMaterial material, ArmorItem.Type slot, Properties properties) {
    this(material, slot, properties, material.getId());
  }

  public MultilayerArmorItem(ModifiableArmorMaterial material, ArmorItem.Type slot, Properties properties, ResourceLocation name) {
    super(material, slot, properties);
  }

  @SuppressWarnings("removal")
  public MultilayerArmorItem(ArmorMaterial material, ArmorItem.Type slot, Properties properties, ToolDefinition toolDefinition) {
    this(material, slot, properties, toolDefinition, ResourceLocation.parse("minecraft:air"));
  }

  public MultilayerArmorItem(ArmorMaterial material, ArmorItem.Type slot, Properties properties, ToolDefinition toolDefinition, ResourceLocation name) {
    super(material, slot, properties, toolDefinition);
  }

  @Nullable
  public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
    return ArmorUtil.getDummyArmorTexture(slot);
  }

  // Client model dispatch disabled in server-focused compatibility build.
}
