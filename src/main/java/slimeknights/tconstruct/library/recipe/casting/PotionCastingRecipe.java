package slimeknights.tconstruct.library.recipe.casting;

import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.IMultiRecipe;
import slimeknights.mantle.recipe.helper.LoadableRecipeSerializer;
import slimeknights.mantle.recipe.helper.TypeAwareRecipeSerializer;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;

import java.util.List;

/**
 * Recipe for casting a fluid onto an item, copying the fluid NBT to the item
 */
public class PotionCastingRecipe implements ICastingRecipe, IMultiRecipe<DisplayCastingRecipe> {
  protected static final LoadableField<FluidIngredient, PotionCastingRecipe> FLUID_FIELD = FluidIngredient.LOADABLE.requiredField("fluid", r -> r.fluid);
  protected static final LoadableField<Integer, PotionCastingRecipe> COOLING_TIME_FIELD = IntLoadable.FROM_ONE.defaultField("cooling_time", 5, r -> r.coolingTime);
  protected static final LoadableField<String, PotionCastingRecipe> GROUP_FIELD = StringLoadable.DEFAULT.defaultField("group", "", r -> r.group);
  public static final RecordLoadable<PotionCastingRecipe> LOADER = RecordLoadable.withLoader(
    ContextKey.ID.requiredField(), GROUP_FIELD,
    IngredientLoadable.DISALLOW_EMPTY.requiredField("bottle", r -> r.bottle),
    FLUID_FIELD,
    Loadables.ITEM.requiredField("result", r -> r.result),
    COOLING_TIME_FIELD,
    (id, group, bottle, fluid, result, coolingTime, loader) -> new PotionCastingRecipe((TypeAwareRecipeSerializer<?>) loader, id, group, bottle, fluid, result, coolingTime));

  @Getter
  protected final TypeAwareRecipeSerializer<?> serializer;
  @Getter
  protected final ResourceLocation id;
  @Getter
  protected final String group;
  /** Input on the casting table, always consumed */
  protected final Ingredient bottle;
  /** Potion ingredient, typically just the potion tag */
  protected final FluidIngredient fluid;
  /** Potion item result, will be given the proper NBT */
  protected final Item result;
  /** Cooling time for this recipe, used for tipped arrows */
  protected final int coolingTime;

  public PotionCastingRecipe(TypeAwareRecipeSerializer<?> serializer, ResourceLocation id, String group, Ingredient bottle, FluidIngredient fluid, Item result, int coolingTime) {
    this.serializer = serializer;
    this.id = id;
    this.group = group;
    this.bottle = bottle;
    this.fluid = fluid;
    this.result = result;
    this.coolingTime = coolingTime;
    CastingRecipeLookup.registerCastable(result);
  }

  @Override
  public RecipeType<?> getType() {
    return serializer.getType();
  }

  @Override
  public boolean matches(ICastingContainer inv, Level level) {
    return bottle.test(inv.getStack()) && fluid.test(inv.getFluid());
  }

  @Override
  public int getFluidAmount(ICastingContainer inv) {
    return fluid.getAmount(inv.getFluid());
  }

  @Override
  public boolean isConsumed() {
    return true;
  }

  @Override
  public boolean switchSlots() {
    return false;
  }

  @Override
  public int getCoolingTime(ICastingContainer inv) {
    return coolingTime;
  }

  public ItemStack assemble(ICastingContainer inv, RegistryAccess access) {
    CompoundTag fluidTag = inv.getFluidTag();
    if (fluidTag != null && fluidTag.contains(PotionUtils.TAG_POTION)) {
      ResourceLocation id = ResourceLocation.tryParse(fluidTag.getString(PotionUtils.TAG_POTION));
      if (id != null) {
        Potion potion = BuiltInRegistries.POTION.getOptional(id).orElse(Potions.WATER.value());
        return PotionUtils.setPotion(new ItemStack(this.result), potion);
      }
    }
    return new ItemStack(this.result);
  }

  @Override
  public ItemStack assemble(ICastingContainer inv, HolderLookup.Provider access) {
    return assemble(inv, (RegistryAccess) access);
  }


  /* JEI */
  protected List<DisplayCastingRecipe> displayRecipes = null;

  @Override
  public List<DisplayCastingRecipe> getRecipes(RegistryAccess access) {
    if (displayRecipes == null) {
      // create a subrecipe for every potion variant
      List<ItemStack> bottles = List.of(bottle.getItems());
      displayRecipes = BuiltInRegistries.POTION.stream()
        .filter(potion -> potion != Potions.WATER.value())
        .map(potion -> {
          ItemStack result = PotionUtils.setPotion(new ItemStack(this.result), potion);
          CompoundTag fluidTag = new CompoundTag();
          fluidTag.putString(PotionUtils.TAG_POTION, Loadables.POTION.getString(potion));
          return new DisplayCastingRecipe(getId(), getType(), bottles, fluid.getFluids().stream()
                                                              .map(fluid -> new FluidStack(fluid.getFluid(), fluid.getAmount()))
                                                              .toList(),
                                          result, coolingTime, true);
        }).toList();
    }
    return displayRecipes;
  }


  /* Recipe interface methods */

  @Override
  public NonNullList<Ingredient> getIngredients() {
    return NonNullList.of(Ingredient.EMPTY, bottle);
  }

  /** @deprecated use {@link #assemble(Container, HolderLookup.Provider)} */
  @Deprecated
  @Override
  public ItemStack getResultItem(HolderLookup.Provider access) {
    return new ItemStack(this.result);
  }

  /** @deprecated kept for older call sites */
  @Deprecated
  public ItemStack getResultItem(RegistryAccess access) {
    return new ItemStack(this.result);
  }
}
