package slimeknights.tconstruct.library.tools.item.armor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.registration.object.IdAwareObject;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Armor material that returns 0 except for name, since we bypass all the usages */
@RequiredArgsConstructor
@Getter
public class DummyArmorMaterial implements IdAwareObject {
  private final ResourceLocation id;
  private final SoundEvent equipSound;
  private ArmorMaterial armorMaterial;

  /** Returns a minimal armor material record for vanilla armor item construction. */
  public ArmorMaterial armorMaterial() {
    if (armorMaterial == null) {
      Map<Type,Integer> defense = new EnumMap<>(Type.class);
      for (Type type : Type.values()) {
        defense.put(type, 0);
      }
      Supplier<Ingredient> repair = () -> Ingredient.EMPTY;
      armorMaterial = new ArmorMaterial(defense, 0, Holder.direct(equipSound), repair, List.of(new ArmorMaterial.Layer(id)), 0, 0);
    }
    return armorMaterial;
  }
}
