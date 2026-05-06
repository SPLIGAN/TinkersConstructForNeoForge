package slimeknights.tconstruct.tools.modifiers.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.common.TinkerEffect;

import java.util.ArrayList;
import java.util.List;

/** Mob effect base that reports no {@linkplain #getCurativeItems() curative items} (milk cannot remove it). */
public class NoMilkEffect extends TinkerEffect {
  public NoMilkEffect(MobEffectCategory typeIn, int color, boolean show) {
    super(typeIn, color, show);
  }

  @Override
  public List<ItemStack> getCurativeItems() {
    return new ArrayList<>();
  }
}
