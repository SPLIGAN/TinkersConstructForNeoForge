package slimeknights.tconstruct.plugin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;

import java.lang.reflect.Method;
import java.util.function.Consumer;

/** Plugin to enable compat with the Diet mod (optional; classpath must not require Diet). */
public final class DietPlugin {
  private DietPlugin() {}

  /** Call on mod construct to enable the compat when Diet is present at runtime. */
  public static void onConstruct() {
    ModifierUtil.foodConsumer = DietPlugin::consumeFood;
  }

  private static void consumeFood(Player player, ItemStack stack, int hunger, float saturation) {
    if (!ModList.get().isLoaded("diet")) {
      return;
    }
    try {
      Class<?> dietCapabilityClass = Class.forName("com.illusivesoulworks.diet.common.capability.DietCapability");
      Method getMethod = dietCapabilityClass.getMethod("get", Player.class);
      Object optional = getMethod.invoke(null, player);
      Method ifPresent = optional.getClass().getMethod("ifPresent", Consumer.class);
      ifPresent.invoke(optional, (Consumer<Object>) cap -> {
        try {
          cap.getClass().getMethod("consume", ItemStack.class, int.class, float.class).invoke(cap, stack, hunger, saturation);
        } catch (ReflectiveOperationException e) {
          throw new RuntimeException(e);
        }
      });
    } catch (ReflectiveOperationException | ClassCastException ignored) {
      // Diet missing or API changed
    }
  }
}
