package slimeknights.tconstruct.library.recipe.ingredient;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.item.IModifiable;

/** Ingredient that only matches tools with a specific hook */
public class ToolHookIngredient implements ICustomIngredient {
  private final TagKey<Item> tag;
  private final ModuleHook<?> hook;

  public static final MapCodec<ToolHookIngredient> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
      ResourceLocation.CODEC.fieldOf("tag")
          .xmap(id -> TagKey.create(Registries.ITEM, id), TagKey::location)
          .forGetter(t -> t.tag),
      ResourceLocation.CODEC.fieldOf("hook").flatXmap(
          ToolHookIngredient::resolveHook,
          h -> DataResult.success(h.getId())
      ).forGetter(t -> t.hook)
  ).apply(inst, ToolHookIngredient::new));

  private static DataResult<ModuleHook<?>> resolveHook(ResourceLocation id) {
    JsonObject o = new JsonObject();
    o.addProperty("hook", id.toString());
    ModuleHook<?> hook = ToolHooks.LOADER.getIfPresent(o, "hook");
    if (hook == null) {
      return DataResult.error(() -> "Unknown tool hook: " + id);
    }
    return DataResult.success(hook);
  }

  protected ToolHookIngredient(TagKey<Item> tag, ModuleHook<?> hook) {
    this.tag = tag;
    this.hook = hook;
  }

  public static Ingredient of(TagKey<Item> tag, ModuleHook<?> hook) {
    return new ToolHookIngredient(tag, hook).toVanilla();
  }

  public static Ingredient of(ModuleHook<?> hook) {
    return of(TinkerTags.Items.MODIFIABLE, hook);
  }

  @Override
  public boolean test(ItemStack stack) {
    return stack.is(tag) && stack.getItem() instanceof IModifiable modifiable
        && modifiable.getToolDefinition().getData().getHooks().hasHook(hook);
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public IngredientType<?> getType() {
    return TinkerIngredientTypes.TOOL_HOOK.get();
  }

  @Override
  public java.util.stream.Stream<ItemStack> getItems() {
    List<ItemStack> list = new ArrayList<>();
    for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
      if (holder.value() instanceof IModifiable modifiable && modifiable.getToolDefinition().getData().getHooks().hasHook(hook)) {
        list.add(new ItemStack(modifiable));
      }
    }
    if (list.isEmpty()) {
      ItemStack marker = new ItemStack(Blocks.BARRIER);
      marker.set(DataComponents.CUSTOM_NAME, Component.literal("Empty Tag: " + tag.location()));
      list.add(marker);
    }
    return list.stream();
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.addProperty(TConstructIngredientJson.NEOFORGE_INGREDIENT_TYPE, TConstruct.getResource("tool_hook").toString());
    json.addProperty("tag", tag.location().toString());
    json.addProperty("hook", hook.getId().toString());
    return json;
  }

  public static ToolHookIngredient parse(JsonObject json) {
    return new ToolHookIngredient(
        Loadables.ITEM_TAG.getOrDefault(json, "tag", TinkerTags.Items.MODIFIABLE),
        ToolHooks.LOADER.getIfPresent(json, "hook")
    );
  }

  public static ToolHookIngredient unwrap(Ingredient ingredient) {
    if (ingredient.isCustom() && ingredient.getCustomIngredient() instanceof ToolHookIngredient t) {
      return t;
    }
    return null;
  }
}
