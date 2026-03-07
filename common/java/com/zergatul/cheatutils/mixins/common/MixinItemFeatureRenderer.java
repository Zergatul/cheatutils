package com.zergatul.cheatutils.mixins.common;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zergatul.cheatutils.extensions.ParametrizedSubmit;
import com.zergatul.cheatutils.modules.esp.EntityEsp;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
            method = "renderSolid",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private void onRenderEntityEspSolidBuffers(
            final SubmitNodeCollection nodeCollection,
            final MultiBufferSource.BufferSource bufferSource,
            final OutlineBufferSource outlineBufferSource,
            CallbackInfo info,
            @Local(ordinal = 0) SubmitNodeStorage.ItemSubmit submit
    ) {
        captureBuffers(submit);
    }

    @Inject(
            method = "renderTranslucent",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
    private void onRenderEntityEspTranslucentBuffers(
            final SubmitNodeCollection nodeCollection,
            final MultiBufferSource.BufferSource bufferSource,
            final OutlineBufferSource outlineBufferSource,
            CallbackInfo info,
            @Local(ordinal = 0) SubmitNodeStorage.ItemSubmit submit
    ) {
        captureBuffers(submit);
    }

    @Unique
    private void captureBuffers(SubmitNodeStorage.ItemSubmit submit) {
        if (submit == null) {
            return;
        }

        ParametrizedSubmit extension = (ParametrizedSubmit) (Object) submit;
        EntityEsp.EntityRenderParameters parameters = extension.getParameters_CU();
        if (parameters == null) {
            return;
        }

        /*RenderType renderType = submission.renderType();
        if (!EntityEsp.instance.isGoodRenderTypeForOverlays(renderType)) {
            return;
        }*/

        BakedQuad quad = submit.quads().stream().findFirst().orElseThrow();
        Identifier texture = quad.spriteInfo().sprite().atlasLocation();

        if (parameters.outlineConfig() != null) {
            VertexConsumer consumer = EntityEsp.instance.getOutlineVertexConsumer(parameters.outlineConfig(), texture);
            ItemRenderer.renderItem(
                    submit.displayContext(),
                    this.poseStack,
                    new OutlineBufferSource() {
                        @Override
                        public @NotNull VertexConsumer getBuffer(RenderType renderType) {
                            return consumer;
                        }
                    },
                    submit.lightCoords(),
                    submit.overlayCoords(),
                    submit.tintLayers(),
                    submit.quads(),
                    ItemStackRenderState.FoilType.NONE);
        }

        if (parameters.overlayConfig() != null) {
            VertexConsumer consumer = EntityEsp.instance.getOverlayVertexConsumer(parameters.overlayConfig(), texture);
            ItemRenderer.renderItem(
                    submit.displayContext(),
                    this.poseStack,
                    new OutlineBufferSource() {
                        @Override
                        public @NotNull VertexConsumer getBuffer(RenderType renderType) {
                            return consumer;
                        }
                    },
                    submit.lightCoords(),
                    submit.overlayCoords(),
                    submit.tintLayers(),
                    submit.quads(),
                    ItemStackRenderState.FoilType.NONE);
        }
    }
}