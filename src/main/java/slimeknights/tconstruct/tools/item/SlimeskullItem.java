package slimeknights.tconstruct.tools.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tools.definition.ModifiableArmorMaterial;
import slimeknights.tconstruct.library.tools.helper.ArmorUtil;
import slimeknights.tconstruct.library.tools.item.armor.ModifiableArmorItem;

import javax.annotation.Nullable;

/** This item is mainly to return the proper model for a slimeskull */
public class SlimeskullItem extends ModifiableArmorItem {
  public SlimeskullItem(ModifiableArmorMaterial material, Properties properties) {
    super(material, ArmorItem.Type.HELMET, properties);
  }

  @Nullable
  public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
    return ArmorUtil.getDummyArmorTexture(slot);
  }

  // Client model dispatch disabled in server-focused compatibility build.
}
