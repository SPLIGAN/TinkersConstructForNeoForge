package slimeknights.tconstruct.fluids.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.tconstruct.fluids.util.ConstantFluidContainerWrapper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class ContainerFoodItem extends Item {
  public ContainerFoodItem(Properties props) {
    super(props);
  }

  public int getUseDuration(ItemStack pStack) {
    return 32;
  }

  public UseAnim getUseAnimation(ItemStack pStack) {
    return UseAnim.DRINK;
  }

  /** Adds effects to the tooltip */
  public static void addEffectTooltip(FoodProperties food, List<Component> tooltip) {
    // 1.21 food effect tooltip API changed; keep base tooltip minimal in this compatibility build.
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
    // skip dynamic food effect tooltip in compatibility build.
  }

  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
    ItemStack container = stack.getCraftingRemainingItem();
    ItemStack result = super.finishUsingItem(stack, level, living);
    Player player = living instanceof Player p ? p : null;
    if (player == null || !player.getAbilities().instabuild) {
      container = container.copy();
      if (result.isEmpty()) {
        return container;
      }
      if (player != null) {
        if (!player.getInventory().add(container)) {
          player.drop(container, false);
        }
      }
    }
    return result;
  }

  public static class FluidContainerFoodItem extends ContainerFoodItem {
    private final Supplier<FluidStack> fluid;
    public FluidContainerFoodItem(Properties props, Supplier<FluidStack> fluid) {
      super(props);
      this.fluid = fluid;
    }

    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
      return new ConstantFluidContainerWrapper(fluid.get(), stack);
    }
  }
}
