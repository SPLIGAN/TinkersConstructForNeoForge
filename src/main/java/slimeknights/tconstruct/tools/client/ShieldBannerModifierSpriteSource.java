package slimeknights.tconstruct.tools.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.registries.Registries;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.sources.LazyLoadedImage;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.materials.MaterialRenderInfo;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Optional;

/** Sprite source creating modifier textures for banners using shield banner textures */
public record ShieldBannerModifierSpriteSource(int cropX, int cropY, int cropWidth, int cropHeight, ResourceLocation destinationPrefix, int offsetX, int offsetY, int outSize) implements SpriteSource {
  private static final Codec<Integer> NON_NEGATIVE = ExtraCodecs.intRange(0, Integer.MAX_VALUE);
  private static final Codec<Integer> SHIELD_SIZE = ExtraCodecs.intRange(0, 64);
  public static final MapCodec<ShieldBannerModifierSpriteSource> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
    SHIELD_SIZE.fieldOf("crop_x").forGetter(ShieldBannerModifierSpriteSource::cropX),
    SHIELD_SIZE.fieldOf("crop_y").forGetter(ShieldBannerModifierSpriteSource::cropY),
    SHIELD_SIZE.fieldOf("crop_width").forGetter(ShieldBannerModifierSpriteSource::cropWidth),
    SHIELD_SIZE.fieldOf("crop_height").forGetter(ShieldBannerModifierSpriteSource::cropHeight),
    ResourceLocation.CODEC.fieldOf("destination_prefix").forGetter(ShieldBannerModifierSpriteSource::destinationPrefix),
    NON_NEGATIVE.fieldOf("offset_x").forGetter(ShieldBannerModifierSpriteSource::offsetX),
    NON_NEGATIVE.fieldOf("offset_y").forGetter(ShieldBannerModifierSpriteSource::offsetY),
    NON_NEGATIVE.fieldOf("output_size").forGetter(ShieldBannerModifierSpriteSource::outSize)
  ).apply(inst, ShieldBannerModifierSpriteSource::new));
  /** JSON / codec parity (field codec from map codec root). */
  public static final Codec<ShieldBannerModifierSpriteSource> CODEC = MAP_CODEC.codec();
  /** Registered type set on init */
  private static SpriteSourceType TYPE = null;

  /** Registers this sprite source */
  @Internal
  public static SpriteSourceType register() {
    if (TYPE == null) {
      TYPE = SpriteSources.register(TConstruct.getResource("shield_banner_to_modifier").toString(), MAP_CODEC);
    }
    return TYPE;
  }

  @Override
  public void run(ResourceManager manager, Output output) {
    // Sheets.SHIELD_MATERIALS is private in 1.21; build shield atlas materials from registered banner patterns (same paths vanilla uses).
    Minecraft mc = Minecraft.getInstance();
    if (mc.level == null) {
      return;
    }
    for (var holder : mc.level.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN).listElements().toList()) {
      ResourceKey<BannerPattern> patternKey = holder.unwrapKey().orElse(null);
      if (patternKey == null) {
        continue;
      }
      ResourceLocation patternId = patternKey.location();
      ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(patternId.getNamespace(), "entity/shield/" + patternId.getPath());
      Material material = new Material(Sheets.SHIELD_SHEET, texture);
      ResourceLocation input = TEXTURE_ID_CONVERTER.idToFile(material.texture());
      Optional<Resource> resource = manager.getResource(input);
      if (resource.isEmpty()) {
        TConstruct.LOG.warn("Unable to find shield texture {} to create modifier sprite", input);
      } else {
        LazyLoadedImage image = new LazyLoadedImage(input, resource.get(), 1);
        ResourceLocation destination = destinationPrefix.withSuffix(MaterialRenderInfo.getSuffix(patternId));
        output.add(destination, new BannerModifierSpriteSupplier(image, input, destination));
      }
    }
  }

  @Override
  public SpriteSourceType type() {
    return register();
  }

  /** Generates a cropped sprite lazily */
  @RequiredArgsConstructor
  private class BannerModifierSpriteSupplier implements SpriteSupplier {
    private final LazyLoadedImage original;
    private final ResourceLocation input, output;

    @Nullable
    @Override
    public SpriteContents apply(SpriteResourceLoader loader) {
      try {
        // its possible the original is bigger than we expect due to HD pack, if so scale it accordingly
        // we only support scaling if it is a multiple of width
        NativeImage original = this.original.get();
        int scale = original.getWidth() / 64;
        if (scale == 0) {
          TConstruct.LOG.warn("Unable to crop {} to produce {} as texture size is less than 64", input, output);
        } else {
          NativeImage generated = new NativeImage(outSize * scale, outSize * scale, true);
          original.copyRect(generated, cropX * scale, cropY * scale, offsetX * scale, offsetY * scale, cropWidth * scale, cropHeight * scale, false, false);
          return new SpriteContents(this.output, new FrameSize(generated.getWidth(), generated.getHeight()), generated, ResourceMetadata.EMPTY);
        }
      } catch (IllegalArgumentException | IOException ex) {
        TConstruct.LOG.warn("Unable to crop {} to produce {}", this.input, this.output, ex);
      } finally {
        this.original.release();
      }
      return null;
    }
  }
}
