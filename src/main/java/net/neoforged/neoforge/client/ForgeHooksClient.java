package net.neoforged.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * Minimal client compatibility shim.
 */
public final class ForgeHooksClient {
  private ForgeHooksClient() {}

  public static BakedModel handleCameraTransforms(PoseStack poseStack, BakedModel model, ItemDisplayContext context, boolean leftHand) {
    return model;
  }
}
