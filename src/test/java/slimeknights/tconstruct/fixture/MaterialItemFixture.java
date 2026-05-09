package slimeknights.tconstruct.fixture;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import slimeknights.tconstruct.library.tools.part.ToolPartItem;
import slimeknights.tconstruct.tools.stats.HandleMaterialStats;
import slimeknights.tconstruct.tools.stats.HeadMaterialStats;
import slimeknights.tconstruct.tools.stats.StatlessMaterialStats;

public class MaterialItemFixture {

  public static ToolPartItem MATERIAL_ITEM, MATERIAL_ITEM_2, MATERIAL_ITEM_HEAD, MATERIAL_ITEM_HANDLE, MATERIAL_ITEM_EXTRA;

  private MaterialItemFixture() {
  }

  private static boolean init = false;
  @SuppressWarnings("unchecked")  // its correct, and if it were to fail this is tests
  public static void init() {
    if (init) {
      return;
    }
    init = true;

    // バニラのアイテムレジストリをアンフリーズ
    ((MappedRegistry<Item>)BuiltInRegistries.ITEM).unfreeze();

    MATERIAL_ITEM = new ToolPartItem(new Item.Properties(), MaterialStatsFixture.STATS_TYPE);
    MATERIAL_ITEM_2 = new ToolPartItem(new Item.Properties(), MaterialStatsFixture.STATS_TYPE_2);
    MATERIAL_ITEM_HEAD = new ToolPartItem(new Item.Properties(), HeadMaterialStats.ID);
    MATERIAL_ITEM_HANDLE = new ToolPartItem(new Item.Properties(), HandleMaterialStats.ID);
    MATERIAL_ITEM_EXTRA = new ToolPartItem(new Item.Properties(), StatlessMaterialStats.BINDING.getIdentifier());

    // アイテムの登録（BuiltInRegistriesを使用）
    // ResourceLocation は 1.21以降であれば ResourceLocation.fromNamespaceAndPath("test", "...")
    register(ResourceLocation.parse("test:test_material"), MATERIAL_ITEM);
    register(ResourceLocation.parse("test:test_material_2"), MATERIAL_ITEM_2);
    register(ResourceLocation.parse("test:test_head"), MATERIAL_ITEM_HEAD);
    register(ResourceLocation.parse("test:test_handle"), MATERIAL_ITEM_HANDLE);
    register(ResourceLocation.parse("test:test_extra"), MATERIAL_ITEM_EXTRA);
    
    // もし NeoForge 独自のレジストリ（例: FluidType）を使いたい場合のみ NeoForgeRegistries を使う
    // NeoForgeRegistries.FLUID_TYPES.register(...) 
  }

  // テスト用の簡易登録メソッド
  private static void register(ResourceLocation name, Item item) {
    Registry.register(BuiltInRegistries.ITEM, name, item);
  }
}
