package slimeknights.tconstruct.shared;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.ClientEventBase;
import slimeknights.tconstruct.library.client.book.TinkerBook;
import slimeknights.tconstruct.library.client.model.UniqueGuiModel;
import slimeknights.tconstruct.library.utils.DomainDisplayName;
import slimeknights.tconstruct.shared.client.FluidParticle;

@EventBusSubscriber(modid = TConstruct.MOD_ID, value = Dist.CLIENT, bus = Bus.MOD)
public class CommonsClientEvents extends ClientEventBase {
  @SubscribeEvent
  static void addResourceListeners(RegisterClientReloadListenersEvent event) {
    DomainDisplayName.addResourceListener(event);
  }

  @SubscribeEvent
  static void registerModelLoaders(RegisterGeometryLoaders event) {
    event.register(TConstruct.getResource("gui"), UniqueGuiModel.LOADER);
  }

  @SubscribeEvent
  static void clientSetup(final FMLClientSetupEvent event) {
    Font unicode = unicodeFontRender();
    TinkerBook.MATERIALS_AND_YOU.fontRenderer = unicode;
    TinkerBook.TINKERS_GADGETRY.fontRenderer = unicode;
    TinkerBook.PUNY_SMELTING.fontRenderer = unicode;
    TinkerBook.MIGHTY_SMELTING.fontRenderer = unicode;
    TinkerBook.FANTASTIC_FOUNDRY.fontRenderer = unicode;
    TinkerBook.ENCYCLOPEDIA.fontRenderer = unicode;
  }

  @SubscribeEvent
  static void registerParticleFactories(RegisterParticleProvidersEvent event) {
    event.registerSpecial(TinkerCommons.fluidParticle.get(), new FluidParticle.Factory());
  }

  private static Font unicodeRenderer;

  /** Gets the unicode font renderer */
  public static Font unicodeFontRender() {
    if (unicodeRenderer == null) {
      unicodeRenderer = new Font(rl -> DefaultFontAccess.uniformSet(Minecraft.getInstance().font), false);
    }

    return unicodeRenderer;
  }

  /**
   * {@link Font#getFontSet} is not accessible from this package; resolve the uniform {@link FontSet} through the
   * vanilla default {@link Font} (public) instead of {@link Minecraft#fontManager}.
   */
  private static final class DefaultFontAccess {
    private static final MethodHandle GET_FONT_SET;

    static {
      try {
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(Font.class, MethodHandles.lookup());
        GET_FONT_SET = lookup.findVirtual(Font.class, "getFontSet", MethodType.methodType(FontSet.class, ResourceLocation.class));
      } catch (IllegalAccessException | NoSuchMethodException e) {
        throw new ExceptionInInitializerError(e);
      }
    }

    private DefaultFontAccess() {}

    static FontSet uniformSet(Font vanillaFont) {
      try {
        return (FontSet) GET_FONT_SET.invokeExact(vanillaFont, Minecraft.UNIFORM_FONT);
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    }
  }
}
