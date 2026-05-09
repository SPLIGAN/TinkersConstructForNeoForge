package slimeknights.tconstruct;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.world.TinkerStructures;
import slimeknights.tconstruct.world.TinkerWorld;
import slimeknights.tconstruct.world.WorldEvents;
import slimeknights.tconstruct.library.TinkerItemDisplays;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.tools.capability.TinkerDataCapability.ComputableDataKey;
import slimeknights.tconstruct.library.tools.capability.TinkerDataCapability.TinkerDataKey;
import slimeknights.tconstruct.library.tools.definition.ToolDefinitionLoader;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayoutLoader;
import slimeknights.mantle.block.entity.InventoryBlockEntity;
import slimeknights.tconstruct.library.utils.Util;
import slimeknights.tconstruct.shared.TinkerAttributes;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.TinkerEffects;
import slimeknights.tconstruct.shared.TinkerMaterials;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.entity.CastingTankBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.ChannelBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.FluidCannonBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.HeaterBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.ProxyTankBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.component.DrainBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.component.DuctBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.component.SmelteryInputOutputBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.component.TankBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.controller.AlloyerBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.controller.HeatingStructureBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.controller.MelterBlockEntity;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.TinkerToolParts;
import slimeknights.tconstruct.tools.TinkerTools;
import slimeknights.tconstruct.library.recipe.ingredient.TinkerIngredientTypes;

import java.util.Locale;
import java.util.Random;
import java.util.function.Supplier;

/**
 * TConstruct, the tool mod. Craft your tools with style, then modify until the original is gone!
 *
 * @author mDiyo
 */

@Mod(TConstruct.MOD_ID)
public class TConstruct {

  public static final String MOD_ID = "tconstruct";
  public static final Logger LOG = LogManager.getLogger(MOD_ID);
  public static final Random RANDOM = new Random();

  /* Instance of this mod, used for grabbing prototype fields */
  public static TConstruct instance;

  /** Mod event bus from the mod constructor; safe for static init that runs after {@link TConstruct} is constructed. */
  public static IEventBus MOD_EVENT_BUS;

  public TConstruct(IEventBus modBus) {
    instance = this;
    MOD_EVENT_BUS = modBus;
    modBus.addListener(TConstruct::commonSetup);
    modBus.addListener(TConstruct::registerCapabilities);
    modBus.addListener(TConstruct::gatherData);
    modBus.register(Sounds.class);
    NeoForge.EVENT_BUS.register(WorldEvents.class);

    Config.init();
    TinkerItemDisplays.init();
    MaterialRegistry.init();

    // initialize modules, done this way rather than with annotations to give us control over the order
    // Registry remaps previously handled via MissingMappingsEvent should migrate to DeferredRegister.addAlias().
    IEventBus bus = modBus;
    // base
    bus.register(new TinkerCommons());
    bus.register(new TinkerMaterials());
    bus.register(new TinkerEffects());
    bus.register(new TinkerAttributes());
    bus.register(new TinkerStructures(bus));
    bus.register(new TinkerWorld());
    // tools
    bus.register(new TinkerTables());
    bus.register(new TinkerModifiers(bus));
    bus.register(new TinkerToolParts());
    bus.register(new TinkerTools());
    // smeltery
    bus.register(new TinkerSmeltery());
    bus.register(new TinkerFluids());

    // init deferred registers
    TinkerIngredientTypes.init(bus);
    TinkerModule.initRegisters(bus);
    TinkerNetwork.setup(bus);
    TinkerTags.init();
    // init client logic
    // Client-only bootstrap is excluded in minimal server-focused build.

    // Optional mod compat (IE/JsonThings/Diet/CraftingTweaks/Dummmmmmy) is disabled in this minimal NeoForge+Arclight build.
  }

  @SubscribeEvent
  static void commonSetup(final FMLCommonSetupEvent event) {
    ToolDefinitionLoader.init();
    StationSlotLayoutLoader.init();
  }

  @SubscribeEvent
  static void registerCapabilities(RegisterCapabilitiesEvent event) {
    TankBlockEntity.registerFluidCapability(event, TinkerSmeltery.tank.get());
    event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TinkerSmeltery.castingTank.get(), (be, ctx) -> ((CastingTankBlockEntity) be).getTank());
    InventoryBlockEntity.registerItemHandlerCapabilities(event, TinkerSmeltery.castingTank.get());
    event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TinkerSmeltery.melter.get(), (be, ctx) -> ((MelterBlockEntity) be).getTank());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerSmeltery.melter.get(), (be, ctx) -> ((MelterBlockEntity) be).getItemHandler());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerSmeltery.smeltery.get(), (be, ctx) -> ((HeatingStructureBlockEntity) be).getMeltingInventory());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerSmeltery.foundry.get(), (be, ctx) -> ((HeatingStructureBlockEntity) be).getMeltingInventory());
    event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TinkerSmeltery.smeltery.get(), (be, ctx) -> ((HeatingStructureBlockEntity) be).getFluidCapability().orElse(null));
    event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TinkerSmeltery.foundry.get(), (be, ctx) -> ((HeatingStructureBlockEntity) be).getFluidCapability().orElse(null));
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerSmeltery.duct.get(), (be, ctx) -> ((DuctBlockEntity) be).getItemHandler());
    event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TinkerSmeltery.channel.get(), (be, ctx) -> ((ChannelBlockEntity) be).getFluidHandlerForSide(ctx));
    event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TinkerSmeltery.drain.get(), (be, ctx) -> ((DrainBlockEntity) be).getExportedFluidHandler());
    event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TinkerSmeltery.alloyer.get(), (be, ctx) -> ((AlloyerBlockEntity) be).getTank());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerSmeltery.heater.get(), (be, ctx) -> ((HeaterBlockEntity) be).getItemHandler());
    event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TinkerSmeltery.fluidCannon.get(), (be, ctx) -> ((FluidCannonBlockEntity) be).getTank());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerSmeltery.fluidCannon.get(), (be, ctx) -> ((FluidCannonBlockEntity) be).getItemHandler());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerSmeltery.chute.get(), (be, ctx) -> ((SmelteryInputOutputBlockEntity.ChuteBlockEntity) be).getExportedItemHandler());
    event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TinkerSmeltery.proxyTank.get(), (be, ctx) -> ((ProxyTankBlockEntity) be).getItemTank());
    event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TinkerSmeltery.proxyTank.get(), (be, ctx) -> ((ProxyTankBlockEntity) be).getItemTank());
  }

  @SubscribeEvent
  static void gatherData(final GatherDataEvent event) {
    // Datagen is intentionally disabled in this server-focused Arclight compatibility build.
  }

  /* Utils */

  /**
   * Gets a resource location for Tinkers
   * @param name  Resource path
   * @return  Location for tinkers
   */
  @SuppressWarnings("removal")
  public static ResourceLocation getResource(String name) {
    return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
  }

  /**
   * Gets a data key for the capability, mainly used for modifier markers
   * @param name  Resource path
   * @return  Location for tinkers
   */
  public static <T> TinkerDataKey<T> createKey(String name) {
    return TinkerDataKey.of(getResource(name));
  }

  /**
   * Gets a data key for the capability, mainly used for modifier markers
   * @param name         Resource path
   * @param constructor  Constructor for compute if absent
   * @return  Location for tinkers
   */
  public static <T> ComputableDataKey<T> createKey(String name, Supplier<T> constructor) {
    return ComputableDataKey.of(getResource(name), constructor);
  }

  /**
   * Returns the given Resource prefixed with tinkers resource location. Use this function instead of hardcoding
   * resource locations.
   */
  public static String resourceString(String res) {
    return String.format("%s:%s", MOD_ID, res);
  }

  /**
   * Prefixes the given unlocalized name with tinkers prefix. Use this when passing unlocalized names for a uniform
   * namespace.
   */
  public static String prefix(String name) {
    return MOD_ID + "." + name.toLowerCase(Locale.US);
  }

  /** Makes a Tinker's description ID */
  public static String makeDescriptionId(String type, String name) {
    return type + "." + MOD_ID + "." + name;
  }

  /**
   * Makes a translation key for the given name
   * @param base  Base name, such as "block" or "gui"
   * @param name  Object name
   * @return  Translation key
   */
  public static String makeTranslationKey(String base, String name) {
    return Util.makeTranslationKey(base, getResource(name));
  }

  /**
   * Makes a translation text component for the given name
   * @param base  Base name, such as "block" or "gui"
   * @param name  Object name
   * @return  Translation key
   */
  public static MutableComponent makeTranslation(String base, String name) {
    return Component.translatable(makeTranslationKey(base, name));
  }

  /**
   * Makes a translation text component for the given name
   * @param base       Base name, such as "block" or "gui"
   * @param name       Object name
   * @param arguments  Additional arguments to the translation
   * @return  Translation key
   */
  public static MutableComponent makeTranslation(String base, String name, Object... arguments) {
    return Component.translatable(makeTranslationKey(base, name), arguments);
  }

  /**
   * This function is called in the constructor in some internal classes that are a common target for addons to wrongly extend.
   * These classes will cause issues if blindly used by the addon, and are typically trivial for the addon to implement
   * the parts they need if they just put in some effort understanding the code they are copying.
   *
   * As a reminder for addon devs, anything that is not in the library package can and will change arbitrarily. If you need to use a feature outside library, request it on our github.
   * @param self  Class to validate
   */
  public static void sealTinkersClass(Object self, String base, String solution) {
    // note for future maintainers: this does not use Java 9's sealed classes as unless you use modules those are restricted to the same package.
    // Dumb restriction but not like we can change it.
    String name = self.getClass().getName();
    if (!name.startsWith("slimeknights.tconstruct.")) {
      throw new IllegalStateException(base + " being extended from invalid package " + name + ". " + solution);
    }
  }
}
