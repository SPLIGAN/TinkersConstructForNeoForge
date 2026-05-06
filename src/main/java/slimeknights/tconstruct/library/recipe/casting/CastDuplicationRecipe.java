package slimeknights.tconstruct.library.recipe.casting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.IMultiRecipe;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.mantle.recipe.helper.TypeAwareRecipeSerializer;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;

import java.util.Arrays;
import java.util.List;

/** Recipe which duplicates the input cast using a fluid */
public class CastDuplicationRecipe extends ItemCastingRecipe implements IMultiRecipe<DisplayCastingRecipe> {
  private static final LoadableField<String, CastDuplicationRecipe> GROUP_FIELD = StringLoadable.DEFAULT.defaultField("group", "", CastDuplicationRecipe::getGroup);
  public static final RecordLoadable<CastDuplicationRecipe> LOADER = RecordLoadable.withLoader(
    ContextKey.ID.requiredField(),
    GROUP_FIELD,
    IngredientLoadable.DISALLOW_EMPTY.requiredField("cast", CastDuplicationRecipe::getCast),
    FLUID_FIELD, COOLING_TIME_FIELD,
    (id, group, cast, fluid, coolingTime, loader) -> new CastDuplicationRecipe((TypeAwareRecipeSerializer<?>) loader, id, group, cast, fluid, coolingTime));

  public CastDuplicationRecipe(TypeAwareRecipeSerializer<?> serializer, ResourceLocation id, String group, Ingredient cast, FluidIngredient fluid, int coolingTime) {
    super(serializer, id, group, cast, fluid, ItemOutput.EMPTY, coolingTime, false, false);
  }

  public ItemStack assemble(ICastingContainer inv, RegistryAccess access) {
    return inv.getStack().copy();
  }

  @Override
  public ItemStack assemble(ICastingContainer inv, HolderLookup.Provider access) {
    return inv.getStack().copy();
  }

  @Override
  public ItemStack getResultItem(HolderLookup.Provider access) {
    ItemStack[] items = getCast().getItems();
    return items.length == 0 ? ItemStack.EMPTY : items[0];
  }

  /** @deprecated kept for older call sites */
  @Deprecated
  public ItemStack getResultItem(RegistryAccess access) {
    ItemStack[] items = getCast().getItems();
    return items.length == 0 ? ItemStack.EMPTY : items[0];
  }

  /* JEI */
  private List<DisplayCastingRecipe> displayRecipes = null;

  @Override
  public List<DisplayCastingRecipe> getRecipes(RegistryAccess access) {
    if (displayRecipes == null) {
      displayRecipes = Arrays.stream(getCast().getItems())
        .map(item -> new DisplayCastingRecipe(getId(), getType(), List.of(item), fluid.getFluids(), item, coolingTime, false))
        .toList();
    }
    return displayRecipes;
  }
}
