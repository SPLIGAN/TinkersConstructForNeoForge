package slimeknights.tconstruct.library.json.predicate.tool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags.Items;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.utils.JsonUtils;

/** Variant of ItemPredicate for matching Tinker tools using {@link ToolStackItemPredicate} */
public class ToolStackItemPredicate {
  public static final ResourceLocation ID = TConstruct.getResource("tool_stack");
  private static final TagKey<Item> MODIFIABLE_TAG = Items.MODIFIABLE;

  private final IJsonPredicate<IToolStackView> predicate;

  private ToolStackItemPredicate(IJsonPredicate<IToolStackView> predicate) {
    this.predicate = predicate;
  }

  public static ItemPredicate ofContext(IJsonPredicate<IToolContext> predicate) {
    return ofTool(ToolStackPredicate.context(predicate));
  }

  public boolean matches(ItemStack stack) {
    // tag check is important to prevent accidently modifying the NBT of non-tools
    return stack.is(Items.MODIFIABLE) && predicate.matches(ToolStack.from(stack));
  }

  public JsonElement serializeToJson() {
    JsonObject json = JsonUtils.withType(ID);
    json.add("predicate", ToolStackPredicate.LOADER.serialize(predicate));
    return json;
  }

  /** Deserializes the tool predicate from JSON */
  public static ItemPredicate ofTool(IJsonPredicate<IToolStackView> predicate) {
    return ItemPredicate.Builder.item().of(MODIFIABLE_TAG).build();
  }

  public static ItemPredicate deserialize(JsonObject json) {
    return ItemPredicate.Builder.item().of(MODIFIABLE_TAG).build();
  }
}
