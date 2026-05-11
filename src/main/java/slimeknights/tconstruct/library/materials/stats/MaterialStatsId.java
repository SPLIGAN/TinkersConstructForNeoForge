package slimeknights.tconstruct.library.materials.stats;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.utils.IdParser;
import slimeknights.tconstruct.library.utils.ResourceId;

import javax.annotation.Nullable;

/**
 * This is just a copy of ResourceLocation for type safety.
 */
public class MaterialStatsId extends ResourceId {
  public static final TypeAdapter<MaterialStatsId> GSON_TYPE_ADAPTER = new TypeAdapter<>() {
    @Override
    public void write(JsonWriter out, MaterialStatsId value) throws IOException {
      out.value(value.toString());
    }

    @Override
    public MaterialStatsId read(JsonReader in) throws IOException {
      return new MaterialStatsId(ResourceLocation.parse(in.nextString()));
    }
  };

  public static final IdParser<MaterialStatsId> PARSER = new IdParser<>(MaterialStatsId::new, "Material Stat Type");

  public MaterialStatsId(String text) {
    super(text);
  }

  public MaterialStatsId(String namespaceIn, String pathIn) {
    super(namespaceIn, pathIn);
  }

  public MaterialStatsId(ResourceLocation location) {
    super(location);
  }

  /** Checks if the given material can be used */
  public boolean canUseMaterial(MaterialId material) {
    return MaterialRegistry.getInstance().getMaterialStats(material.getId(), this).isPresent();
  }


  /** {@return Material Stats ID, or null if invalid} */
  @Nullable
  public static MaterialStatsId tryParse(String string) {
    return tryParse(string, MaterialStatsId::new);
  }

  /** {@return Material Stats ID, or null if invalid} */
  @Nullable
  public static MaterialStatsId tryBuild(String namespace, String path) {
    return tryBuild(namespace, path, MaterialStatsId::new);
  }
}
