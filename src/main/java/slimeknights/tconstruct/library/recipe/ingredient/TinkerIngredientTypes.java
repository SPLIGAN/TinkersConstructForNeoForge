package slimeknights.tconstruct.library.recipe.ingredient;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import slimeknights.tconstruct.TConstruct;

/** Deferred registration for custom {@link net.neoforged.neoforge.common.crafting.ICustomIngredient} types (NeoForge 21+). */
public final class TinkerIngredientTypes {
  private TinkerIngredientTypes() {}

  public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
      DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, TConstruct.MOD_ID);

  public static final DeferredHolder<IngredientType<?>, IngredientType<NoContainerIngredient>> NO_CONTAINER =
      INGREDIENT_TYPES.register("no_container", () -> new IngredientType<>(NoContainerIngredient.MAP_CODEC));

  public static final DeferredHolder<IngredientType<?>, IngredientType<MaterialIngredient>> MATERIAL =
      INGREDIENT_TYPES.register("material", () -> new IngredientType<>(MaterialIngredient.MAP_CODEC));

  public static final DeferredHolder<IngredientType<?>, IngredientType<MaterialValueIngredient>> MATERIAL_VALUE =
      INGREDIENT_TYPES.register("material_value", () -> new IngredientType<>(MaterialValueIngredient.MAP_CODEC));

  public static final DeferredHolder<IngredientType<?>, IngredientType<ToolHookIngredient>> TOOL_HOOK =
      INGREDIENT_TYPES.register("tool_hook", () -> new IngredientType<>(ToolHookIngredient.MAP_CODEC));

  public static void init(IEventBus modBus) {
    INGREDIENT_TYPES.register(modBus);
  }
}
