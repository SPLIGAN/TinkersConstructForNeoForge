package slimeknights.tconstruct.library.recipe.ingredient;

import java.util.Arrays;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

/** Base for custom ingredients that delegate matching to a vanilla {@link Ingredient}. */
public abstract class NestedIngredient implements ICustomIngredient {
  protected final Ingredient nested;

  protected NestedIngredient(Ingredient nested) {
    this.nested = nested;
  }

  @Override
  public boolean test(ItemStack stack) {
    return nested.test(stack);
  }

  @Override
  public Stream<ItemStack> getItems() {
    return Arrays.stream(nested.getItems());
  }

  @Override
  public boolean isSimple() {
    return nested.isSimple();
  }

  protected Ingredient nested() {
    return nested;
  }

  @Override
  public abstract IngredientType<?> getType();
}
