package slimeknights.tconstruct.library.recipe.casting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.TypeAwareRecipeSerializer;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.mantle.util.RetexturedHelper;

/** Extension of item recipe that sets the result block to the input block */
public class RetexturedCastingRecipe extends ItemCastingRecipe {
  private static final LoadableField<String, RetexturedCastingRecipe> GROUP_FIELD = StringLoadable.DEFAULT.defaultField("group", "", RetexturedCastingRecipe::getGroup);
  /** Loader instance */
  public static final RecordLoadable<RetexturedCastingRecipe> LOADER = RecordLoadable.withLoader(
    ContextKey.ID.requiredField(),
    GROUP_FIELD, CAST_FIELD, FLUID_FIELD, RESULT_FIELD, COOLING_TIME_FIELD, CAST_CONSUMED_FIELD, SWITCH_SLOTS_FIELD,
    (id, group, cast, fluid, result, coolingTime, consumed, switchSlots, loader) -> new RetexturedCastingRecipe((TypeAwareRecipeSerializer<?>) loader, id, group, cast, fluid, result, coolingTime, consumed, switchSlots));

  public RetexturedCastingRecipe(TypeAwareRecipeSerializer<?> serializer, ResourceLocation id, String group, Ingredient cast, FluidIngredient fluid, ItemOutput result, int coolingTime, boolean consumed, boolean switchSlots) {
    super(serializer, id, group, cast, fluid, result, coolingTime, consumed, switchSlots);
  }

  @Override
  public ItemStack assemble(ICastingContainer inv, HolderLookup.Provider access) {
    ItemStack result = getResultItem(access).copy();
    if (inv.getStack().getItem() instanceof BlockItem blockItem ) {
      return RetexturedHelper.setTexture(result, blockItem.getBlock());
    }
    return result;
  }

  /** @deprecated kept for older call sites */
  @Deprecated
  public ItemStack assemble(ICastingContainer inv, RegistryAccess access) {
    return assemble(inv, (HolderLookup.Provider) access);
  }
}
