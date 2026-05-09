package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import java.util.Arrays;
import java.util.List;
import net.neoforged.neoforge.common.conditions.ICondition;

/** NeoForge 21+ replacement for removed CraftingHelper condition JSON helpers. */
public final class TConstructConditionJson {
  private TConstructConditionJson() {}

  public static JsonElement serialize(ICondition condition) {
    return ICondition.CODEC.encodeStart(JsonOps.INSTANCE, condition)
        .getOrThrow(JsonParseException::new);
  }

  public static JsonElement serializeList(List<ICondition> conditions) {
    return ICondition.LIST_CODEC.encodeStart(JsonOps.INSTANCE, conditions)
        .getOrThrow(JsonParseException::new);
  }

  public static JsonElement serializeArray(ICondition... conditions) {
    return serializeList(Arrays.asList(conditions));
  }

  public static ICondition parseCondition(JsonElement element) {
    return ICondition.CODEC.parse(JsonOps.INSTANCE, element)
        .getOrThrow(JsonParseException::new);
  }

  /** Backward-compatible alias for older callsites. */
  public static ICondition parse(JsonElement element) {
    return parseCondition(element);
  }

  /** Parses a single condition from a JSON object that has a {@code condition} member. */
  public static ICondition parseConditionField(com.google.gson.JsonObject json) {
    return parseCondition(json.get("condition"));
  }

  public static List<ICondition> parseConditionList(JsonElement element) {
    return ICondition.LIST_CODEC.parse(JsonOps.INSTANCE, element)
        .getOrThrow(JsonParseException::new);
  }

}
