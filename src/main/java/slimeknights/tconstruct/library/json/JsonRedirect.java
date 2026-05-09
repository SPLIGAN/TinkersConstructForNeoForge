package slimeknights.tconstruct.library.json;

import com.google.gson.JsonObject;
import lombok.Data;
import net.minecraft.resources.ResourceLocation;
import slimeknights.tconstruct.library.recipe.ingredient.TConstructConditionJson;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;

/** Represents a redirect in a material or modifier JSON */
@SuppressWarnings("ClassCanBeRecord") // GSON does not support records
@Data
public class JsonRedirect {
  private final ResourceLocation id;
  @Nullable
  private final ICondition condition;

  public JsonRedirect(ResourceLocation id, @Nullable ICondition condition) {
    this.id = id;
    this.condition = condition;
  }

  public ResourceLocation getId() {
    return id;
  }

  @Nullable
  public ICondition getCondition() {
    return condition;
  }

  /** Serializes this to JSON */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("id", id.toString());
    if (condition != null) {
      json.add("condition", TConstructConditionJson.serialize(condition));
    }
    return json;
  }

  /** Deserializes this to JSON */
  public static JsonRedirect fromJson(JsonObject json) {
    ResourceLocation id = JsonHelper.getResourceLocation(json, "id");
    ICondition condition = null;
    if (json.has("condition")) {
      condition = TConstructConditionJson.parseConditionField(json);
    }
    return new JsonRedirect(id, condition);
  }
}
