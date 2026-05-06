package slimeknights.tconstruct.fluids.fluids;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.mantle.fluid.texture.ClientTextureFluidType;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.tconstruct.fluids.TinkerFluids;

import java.util.function.Consumer;

public class PotionFluidType extends FluidType {
  public PotionFluidType(Properties properties) {
    super(properties);
  }

  @Override
  public String getDescriptionId(FluidStack stack) {
    Potion potion = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().map(holder -> holder.value()).orElse(Potions.WATER.value());
    return Potion.getName(java.util.Optional.of(BuiltInRegistries.POTION.wrapAsHolder(potion)), "item.minecraft.potion.effect.");
  }

  @Override
  public ItemStack getBucket(FluidStack fluidStack) {
    ItemStack itemStack = new ItemStack(fluidStack.getFluid().getBucket());
    Potion potion = fluidStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().map(holder -> holder.value()).orElse(Potions.WATER.value());
    if (itemStack.is(Items.POTION) || itemStack.is(Items.SPLASH_POTION) || itemStack.is(Items.LINGERING_POTION)) {
      PotionUtils.setPotion(itemStack, potion);
    }
    return itemStack;
  }

  @Override
  public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
    consumer.accept(new ClientTextureFluidType(this) {
      /**
       * Gets the color, based on {@link PotionUtils#getColor(ItemStack)}
       * @param stack  Fluid stack instance
       * @return  Color for the fluid
       */
      @Override
      public int getTintColor(FluidStack stack) {
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if (contents.equals(PotionContents.EMPTY) || contents.potion().map(holder -> holder.value()).orElse(Potions.WATER.value()) == Potions.WATER.value()) {
          return getTintColor();
        }
        return contents.getColor() | 0xFF000000;
      }
    });
  }

  /** Creates a fluid stack for the given potion */
  public static FluidStack potionFluid(ResourceKey<Potion> potion, int size) {
    FluidStack stack = new FluidStack(TinkerFluids.potion.get(), size);
    if (potion != Potions.WATER.unwrapKey().orElseThrow()) {
      stack.set(DataComponents.POTION_CONTENTS, new PotionContents(BuiltInRegistries.POTION.getHolderOrThrow(potion)));
    }
    return stack;
  }

  /** Creates a fluid stack for the given potion */
  @SuppressWarnings("deprecation")  // forge registries have nullable keys, like why would you want that?
  public static FluidStack potionFluid(Potion potion, int size) {
    FluidStack stack = new FluidStack(TinkerFluids.potion.get(), size);
    if (potion != Potions.WATER.value()) {
      stack.set(DataComponents.POTION_CONTENTS, new PotionContents(BuiltInRegistries.POTION.wrapAsHolder(potion)));
    }
    return stack;
  }

  /** Creates a fluid output for the given potion */
  @SuppressWarnings("deprecation")  // forge registries have nullable keys, like why would you want that?
  public static FluidOutput potionResult(Potion potion, int size) {
    return FluidOutput.fromStack(potionFluid(potion, size));
  }

  /** Creates a potion bucket for the given potion */
  public static ItemStack potionBucket(ResourceKey<Potion> potion) {
    ItemStack stack = new ItemStack(TinkerFluids.potion);
    if (potion != Potions.WATER.unwrapKey().orElseThrow()) {
      PotionUtils.setPotion(stack, BuiltInRegistries.POTION.getHolderOrThrow(potion));
    }
    return stack;
  }

  /** Creates a potion bucket for the given potion */
  @SuppressWarnings("deprecation")  // forge registries have nullable keys, like why would you want that?
  public static ItemStack potionBucket(Potion potion) {
    ItemStack stack = new ItemStack(TinkerFluids.potion);
    if (potion != Potions.WATER.value()) {
      PotionUtils.setPotion(stack, BuiltInRegistries.POTION.wrapAsHolder(potion));
    }
    return stack;
  }
}
