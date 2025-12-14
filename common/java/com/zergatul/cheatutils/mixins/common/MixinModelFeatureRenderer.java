package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zergatul.cheatutils.extensions.ParametrizedSubmit;
import com.zergatul.cheatutils.modules.esp.EntityEsp;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelFeatureRenderer.class)
public abstract class MixinModelFeatureRenderer {

    @Shadow
    @Final
    private PoseStack poseStack;

    @Inject(
            method = "renderModel",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private void onRenderEntityEspBuffers(
            SubmitNodeStorage.ModelSubmit<?> modelSubmission,
            RenderType renderType,
            VertexConsumer vertexConsumer,
            OutlineBufferSource outlineBufferSource,
            MultiBufferSource.BufferSource bufferSource,
            CallbackInfo ci
    ) {
        ParametrizedSubmit extension = (ParametrizedSubmit) (Object) modelSubmission;
        EntityEsp.EntityRenderParameters parameters = extension.getParameters_CU();
        if (parameters == null) {
            return;
        }

        if (!EntityEsp.instance.isGoodRenderTypeForOverlays(renderType)) {
            return;
        }

        ResourceLocation texture;
        if (modelSubmission.sprite() != null) {
            texture = modelSubmission.sprite().atlasLocation();
        } else {
            texture = EntityEsp.instance.getTextureFromRenderType(renderType).orElse(null);
        }

        if (texture == null) {
            return;
        }

        if (parameters.outlineConfig() != null) {
            VertexConsumer consumer = EntityEsp.instance.getOutlineVertexConsumer(parameters.outlineConfig(), texture);
            modelSubmission.model().renderToBuffer(
                    this.poseStack,
                    modelSubmission.sprite() != null ? modelSubmission.sprite().wrap(consumer) : consumer,
                    modelSubmission.lightCoords(),
                    modelSubmission.overlayCoords(),
                    modelSubmission.tintedColor());
        }

        if (parameters.overlayConfig() != null) {
            VertexConsumer consumer = EntityEsp.instance.getOverlayVertexConsumer(parameters.overlayConfig(), texture);
            modelSubmission.model().renderToBuffer(
                    this.poseStack,
                    modelSubmission.sprite() != null ? modelSubmission.sprite().wrap(consumer) : consumer,
                    modelSubmission.lightCoords(),
                    modelSubmission.overlayCoords(),
                    modelSubmission.tintedColor());
        }
    }
}