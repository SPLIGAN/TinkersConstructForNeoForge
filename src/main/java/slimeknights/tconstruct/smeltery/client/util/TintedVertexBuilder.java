package slimeknights.tconstruct.smeltery.client.util;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Vertex builder wrapper that tints all quads passed in
 */
public final class TintedVertexBuilder implements VertexConsumer {
  private final VertexConsumer inner;
  private final float tr, tg, tb, ta;

  public TintedVertexBuilder(VertexConsumer inner, int tintRed, int tintGreen, int tintBlue, int tintAlpha) {
    this.inner = inner;
    this.tr = tintRed / 255f;
    this.tg = tintGreen / 255f;
    this.tb = tintBlue / 255f;
    this.ta = tintAlpha / 255f;
  }

  @Override
  public VertexConsumer addVertex(float x, float y, float z) {
    return inner.addVertex(x, y, z);
  }

  @Override
  public VertexConsumer addVertex(Matrix4f pose, float x, float y, float z) {
    return inner.addVertex(pose, x, y, z);
  }

  @Override
  public VertexConsumer addVertex(Pose pose, float x, float y, float z) {
    return inner.addVertex(pose, x, y, z);
  }

  @Override
  public VertexConsumer addVertex(Pose pose, Vector3f pos) {
    return inner.addVertex(pose, pos);
  }

  @Override
  public VertexConsumer addVertex(Vector3f pos) {
    return inner.addVertex(pos);
  }

  @Override
  public void addVertex(float x, float y, float z, int color, float u, float v, int packedOverlay, int packedLight, float normalX, float normalY, float normalZ) {
    int a = (color >>> 24) & 0xFF;
    int r = (color >> 16) & 0xFF;
    int g = (color >> 8) & 0xFF;
    int b = color & 0xFF;
    int tinted = ((int) (a * ta) << 24) | ((int) (r * tr) << 16) | ((int) (g * tg) << 8) | (int) (b * tb);
    inner.addVertex(x, y, z, tinted, u, v, packedOverlay, packedLight, normalX, normalY, normalZ);
  }

  @Override
  public VertexConsumer setColor(int red, int green, int blue, int alpha) {
    return inner.setColor((int) (red * tr), (int) (green * tg), (int) (blue * tb), (int) (alpha * ta));
  }

  @Override
  public VertexConsumer setColor(float red, float green, float blue, float alpha) {
    return inner.setColor(red * tr, green * tg, blue * tb, alpha * ta);
  }

  @Override
  public VertexConsumer setColor(int color) {
    int a = (color >>> 24) & 0xFF;
    int r = (color >> 16) & 0xFF;
    int g = (color >> 8) & 0xFF;
    int b = color & 0xFF;
    int tinted = ((int) (a * ta) << 24) | ((int) (r * tr) << 16) | ((int) (g * tg) << 8) | (int) (b * tb);
    return inner.setColor(tinted);
  }

  @Override
  public VertexConsumer setWhiteAlpha(int alpha) {
    return inner.setWhiteAlpha((int) (alpha * ta));
  }

  @Override
  public VertexConsumer setUv(float u, float v) {
    return inner.setUv(u, v);
  }

  @Override
  public VertexConsumer setUv1(int u, int v) {
    return inner.setUv1(u, v);
  }

  @Override
  public VertexConsumer setUv2(int u, int v) {
    return inner.setUv2(u, v);
  }

  @Override
  public VertexConsumer setOverlay(int packedOverlay) {
    return inner.setOverlay(packedOverlay);
  }

  @Override
  public VertexConsumer setLight(int packedLight) {
    return inner.setLight(packedLight);
  }

  @Override
  public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
    return inner.setNormal(normalX, normalY, normalZ);
  }

  @Override
  public VertexConsumer setNormal(Pose pose, float normalX, float normalY, float normalZ) {
    return inner.setNormal(pose, normalX, normalY, normalZ);
  }

  @Override
  public void putBulkData(Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
    inner.putBulkData(pose, quad, red * tr, green * tg, blue * tb, alpha * ta, packedLight, packedOverlay);
  }

  @Override
  public void putBulkData(Pose pose, BakedQuad quad, float[] brightness, float red, float green, float blue, float alpha, int[] lightmap, int packedOverlay, boolean readAlpha) {
    inner.putBulkData(pose, quad, brightness, red * tr, green * tg, blue * tb, alpha * ta, lightmap, packedOverlay, readAlpha);
  }

  @Override
  public void putBulkData(Pose pose, BakedQuad bakedQuad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay, boolean readExistingColor) {
    inner.putBulkData(pose, bakedQuad, red * tr, green * tg, blue * tb, alpha * ta, packedLight, packedOverlay, readExistingColor);
  }

  @Override
  public VertexConsumer misc(VertexFormatElement element, int... rawData) {
    return inner.misc(element, rawData);
  }
}
