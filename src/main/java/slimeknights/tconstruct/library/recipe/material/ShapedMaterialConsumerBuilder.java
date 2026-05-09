package slimeknights.tconstruct.library.recipe.material;

import lombok.NoArgsConstructor;
import net.minecraft.data.recipes.RecipeOutput;
import slimeknights.mantle.recipe.data.ConsumerWrapperBuilder;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import java.util.ArrayList;
import java.util.List;

/** Special variant of {@link ConsumerWrapperBuilder} for {@link ShapedMaterialRecipe} */
@Deprecated
@NoArgsConstructor(staticName = "wrap")
public class ShapedMaterialConsumerBuilder {
  private final List<MaterialVariantId> materials = new ArrayList<>();

  /** Adds a material to the builder */
  public ShapedMaterialConsumerBuilder material(MaterialVariantId material) {
    materials.add(material);
    return this;
  }

  /** Builds the wrapped consumer */
  public RecipeOutput build(RecipeOutput consumer) {
    return consumer;
  }
}
