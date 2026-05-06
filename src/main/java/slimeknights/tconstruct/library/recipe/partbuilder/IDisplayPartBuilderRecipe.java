package slimeknights.tconstruct.library.recipe.partbuilder;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.client.SafeClientAccess;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeCache;

import java.util.List;

/**
 * Part builder recipes that can show in JEI.
 * This still extends {@link IPartBuilderRecipe} so display code can reuse the core recipe contract.
 */
public interface IDisplayPartBuilderRecipe extends IPartBuilderRecipe {
  /**
   * Gets the material variant required to craft this recipe.
   * Uses {@link MaterialVariant} because display recipes may still need the resolved variant payload, not just the ID.
   */
  MaterialVariant getMaterial();

  /** Gets a list of input material items for display in the material slot. */
  default List<ItemStack> getMaterialItems() {
    MaterialVariant material = getMaterial();
    if (material.isUnknown()) {
      return List.of();
    }
    return MaterialRecipeCache.getItems(material.getVariant());
  }

  /** Gets a list of results. Should either be size 1, or size matching {@link #getMaterialItems()} */
  default List<ItemStack> getResultItems() {
    RegistryAccess access = SafeClientAccess.getRegistryAccess();
    return access == null ? List.of() : List.of(getResultItem(access));
  }

  /**
   * Gets a list of pattern items to display in the pattern slot
   * @return  Pattern items
   */
  default List<ItemStack> getPatternItems() {
    return RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, TinkerTags.Items.DEFAULT_PATTERNS).map(ItemStack::new).toList();
  }
}
