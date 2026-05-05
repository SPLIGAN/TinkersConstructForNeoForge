/*
 * Derived from Forge ConditionalAdvancement (MPL/LGPL); adapted for NeoForge ICondition codecs.
 */
package slimeknights.tconstruct.common.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Writes Forge-style conditional advancement JSON understood by NeoForge loaders. */
public final class ConditionalAdvancement {
  private ConditionalAdvancement() {}

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private final List<ICondition[]> conditions = new ArrayList<>();
    private final List<Supplier<JsonElement>> advancements = new ArrayList<>();

    private List<ICondition> currentConditions = new ArrayList<>();
    private boolean locked;

    public Builder addCondition(ICondition condition) {
      if (locked) {
        throw new IllegalStateException("Attempted to modify finished builder");
      }
      currentConditions.add(condition);
      return this;
    }

    /**
     * Serializes an advancement builder using {@link Advancement.Builder#build(ResourceLocation)}.
     */
    public Builder addAdvancement(ResourceLocation advancementId, Advancement.Builder advancement) {
      if (locked) {
        throw new IllegalStateException("Attempted to modify finished builder");
      }
      if (currentConditions.isEmpty()) {
        throw new IllegalStateException("Cannot add an advancement with no conditions.");
      }
      conditions.add(currentConditions.toArray(ICondition[]::new));
      advancements.add(() -> advancement.build(advancementId).value().deconstruct().serializeToJson());
      currentConditions.clear();
      return this;
    }

    public JsonObject write() {
      if (!locked) {
        if (!currentConditions.isEmpty()) {
          throw new IllegalStateException("Invalid builder state: orphaned conditions");
        }
        if (advancements.isEmpty()) {
          throw new IllegalStateException("Invalid builder state: no advancements");
        }
        locked = true;
      }
      JsonObject json = new JsonObject();
      JsonArray array = new JsonArray();
      json.add("advancements", array);
      for (int x = 0; x < conditions.size(); x++) {
        JsonObject holder = new JsonObject();
        JsonArray conds = new JsonArray();
        for (ICondition c : conditions.get(x)) {
          conds.add(ICondition.CODEC.encodeStart(JsonOps.INSTANCE, c).getOrThrow(IllegalStateException::new));
        }
        holder.add("conditions", conds);
        holder.add("advancement", advancements.get(x).get());
        array.add(holder);
      }
      return json;
    }
  }
}
