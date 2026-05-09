package slimeknights.tconstruct.library.recipe.material;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.data.recipes.RecipeOutput;
import slimeknights.mantle.recipe.data.ConsumerWrapperBuilder;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import java.util.ArrayList;
import java.util.List;

/** Special variant of {@link ConsumerWrapperBuilder} for {@link ShapedMaterialsRecipe} and {@link ShapelessMaterialsRecipe} */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MaterialsConsumerBuilder {
  private final String parts;
  private final int partCount;
  private final List<MaterialVariantId> materials = new ArrayList<>();

  /** Creates a new shaped recipe with the given ingredients as parts */
  public static MaterialsConsumerBuilder shaped(String parts) {
    if (parts.isEmpty()) {
      throw new IllegalArgumentException("Parts may not be empty");
    }
    return new MaterialsConsumerBuilder(parts, 0);
  }

  /** Creates a new shapeless recipe with the first ingredients as parts */
  public static MaterialsConsumerBuilder shapeless(int parts) {
    if (parts <= 0) {
      throw new IllegalArgumentException("Parts must be greater than 0");
    }
    return new MaterialsConsumerBuilder("", parts);
  }

  /** Adds a material to the builder */
  public MaterialsConsumerBuilder material(MaterialVariantId material) {
    materials.add(material);
    return this;
  }

  /** Builds the wrapped consumer */
  public RecipeOutput build(RecipeOutput consumer) {
    return consumer;
  }
}
