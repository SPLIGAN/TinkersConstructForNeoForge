package slimeknights.tconstruct.common.recipe.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Minimal recipe holder for datagen when only the serializer JSON is needed (Mantle helper replacement).
 */
public record SimpleFinishedRecipe(ResourceLocation id, RecipeSerializer<?> serializer, RecipeType<?> recipeType) implements Recipe<RecipeInput> {

  @Override
  public boolean matches(RecipeInput container, Level level) {
    return false;
  }

  @Override
  public ItemStack assemble(RecipeInput container, HolderLookup.Provider registries) {
    return ItemStack.EMPTY;
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return false;
  }

  @Override
  public ItemStack getResultItem(HolderLookup.Provider registries) {
    return ItemStack.EMPTY;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return serializer;
  }

  @Override
  public RecipeType<?> getType() {
    return recipeType;
  }
}
