package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zergatul.cheatutils.extensions.ParametrizedSubmit;
import com.zergatul.cheatutils.modules.esp.EntityEsp;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;

@Mixin(ItemFeatureRenderer.class)
public abstract class MixinItemFeatureRenderer {

    @Shadow
    @Final
    private PoseStack poseStack;

    @Inject(
            method = "render",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"),
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onRenderEntityEspBuffers(
            SubmitNodeCollection submitNodeCollection,
            MultiBufferSource.BufferSource bufferSource,
            OutlineBufferSource outlineBufferSource,
            CallbackInfo info,
            Iterator<?> iterator,
            SubmitNodeStorage.ItemSubmit submission
    ) {
        if (submission == null) {
            return;
        }

        ParametrizedSubmit extension = (ParametrizedSubmit) (Object) submission;
        EntityEsp.EntityRenderParameters parameters = extension.getParameters_CU();
        if (parameters == null) {
            return;
        }

        RenderType renderType = submission.renderType();
        if (!EntityEsp.instance.isGoodRenderTypeForOverlays(renderType)) {
            return;
        }

        Identifier texture = EntityEsp.instance.getTextureFromRenderType(renderType).orElse(null);
        if (texture == null) {
            return;
        }

        if (parameters.outlineConfig() != null) {
            VertexConsumer consumer = EntityEsp.instance.getOutlineVertexConsumer(parameters.outlineConfig(), texture);
            ItemRenderer.renderItem(
                    submission.displayContext(),
                    this.poseStack,
                    new OutlineBufferSource() {
                        @Override
                        public @NotNull VertexConsumer getBuffer(RenderType renderType) {
                            return consumer;
                        }
                    },
                    submission.lightCoords(),
                    submission.overlayCoords(),
                    submission.tintLayers(),
                    submission.quads(),
                    submission.renderType(),
                    ItemStackRenderState.FoilType.NONE);
        }

        if (parameters.overlayConfig() != null) {
            VertexConsumer consumer = EntityEsp.instance.getOverlayVertexConsumer(parameters.overlayConfig(), texture);
            ItemRenderer.renderItem(
                    submission.displayContext(),
                    this.poseStack,
                    new OutlineBufferSource() {
                        @Override
                        public @NotNull VertexConsumer getBuffer(RenderType renderType) {
                            return consumer;
                        }
                    },
                    submission.lightCoords(),
                    submission.overlayCoords(),
                    submission.tintLayers(),
                    submission.quads(),
                    submission.renderType(),
                    ItemStackRenderState.FoilType.NONE);
        }
    }
}