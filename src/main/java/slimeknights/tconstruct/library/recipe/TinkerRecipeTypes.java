package slimeknights.tconstruct.library.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import slimeknights.tconstruct.TConstruct;

/**
 * Class containing all of Tinkers Construct recipe types
 */
public class TinkerRecipeTypes {
  /** Deferred instance */
  private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, TConstruct.MOD_ID);

  public static final DeferredHolder<RecipeType<?>, RecipeType<?>> PART_BUILDER = register("part_builder");
  public static final DeferredHolder<RecipeType<?>, RecipeType<?>> MATERIAL = register("material");
  public static final DeferredHolder<RecipeType<?>, RecipeType<?>> TINKER_STATION = register("tinker_station");
  public static final DeferredHolder<RecipeType<?>, RecipeType<?>> MODIFIER_WORKTABLE = register("modifier_worktable");

  // casting
  public static final DeferredHolder<RecipeType<?>, RecipeType<?>> CASTING_BASIN = register("casting_basin");
  public static final DeferredHolder<RecipeType<?>, RecipeType<?>> CASTING_TABLE = register("casting_table");
  public static final DeferredHolder<RecipeType<?>, RecipeType<?>> MOLDING_TABLE = register("molding_table");
  public static final DeferredHolder<RecipeType<?>, RecipeType<?>> MOLDING_BASIN = register("molding_basin");

  // smeltery
  public static final DeferredHolder<RecipeType<?>, RecipeType<?>> MELTING = register("melting");
  public static final DeferredHolder<RecipeType<?>, RecipeType<?>> ENTITY_MELTING = register("entity_melting");
  public static final DeferredHolder<RecipeType<?>, RecipeType<?>> FUEL = register("fuel");
  public static final DeferredHolder<RecipeType<?>, RecipeType<?>> ALLOYING = register("alloying");

  // modifiers
  public static final DeferredHolder<RecipeType<?>, RecipeType<?>> SEVERING = register("severing");

  /** Internal recipe type for recipes that are not pulled by any specific crafting block */
  public static final DeferredHolder<RecipeType<?>, RecipeType<?>> DATA = register("data");

  /** Initializes the deferred register */
  public static void init(IEventBus bus) {
    TYPES.register(bus);
  }

  /**
   * Registers a new recipe type, prefixing with the mod ID
   * @param name  Recipe type name
   * @param <T>   Recipe type
   * @return  Registered recipe type
   */
  static DeferredHolder<RecipeType<?>, RecipeType<?>> register(String name) {
    return TYPES.register(name, () -> new RecipeType<>() {
      @Override
      public String toString() {
        return TConstruct.MOD_ID + ":" + name;
      }
    });
  }
}
