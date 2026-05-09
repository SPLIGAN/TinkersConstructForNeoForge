package slimeknights.mantle.recipe.helper;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import javax.annotation.Nullable;

/**
 * Back-compat serializer helper used by older Tinkers recipe serializers.
 * Keeps the old API surface while delegating to vanilla serializers.
 */
public interface LoggingRecipeSerializer<T extends Recipe<?>> extends RecipeSerializer<T> {
  RecipeSerializer<ShapedRecipe> SHAPED_RECIPE = RecipeSerializer.SHAPED_RECIPE;
  RecipeSerializer<ShapelessRecipe> SHAPELESS_RECIPE = RecipeSerializer.SHAPELESS_RECIPE;

  @Nullable
  T fromNetworkSafe(ResourceLocation recipeId, FriendlyByteBuf buffer);

  void toNetworkSafe(FriendlyByteBuf buffer, T recipe);

  default T fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
    return fromNetworkSafe(recipeId, buffer);
  }

  default void toNetwork(FriendlyByteBuf buffer, T recipe) {
    toNetworkSafe(buffer, recipe);
  }

  T fromJson(ResourceLocation recipeId, JsonObject json);

  @Override
  default MapCodec<T> codec() {
    throw new UnsupportedOperationException("Legacy serializer must override codec() for data loading");
  }

  @Override
  default StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
    return StreamCodec.of(this::toNetworkSafe, buffer -> fromNetworkSafe(ResourceLocation.fromNamespaceAndPath("minecraft", "missingno"), buffer));
  }
}
