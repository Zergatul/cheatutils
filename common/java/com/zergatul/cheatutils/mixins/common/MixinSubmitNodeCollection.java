package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.extensions.EntityRenderStateExtension;
import com.zergatul.cheatutils.extensions.SubmitNodeStorageModelSubmitExtension;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SubmitNodeCollection.class)
public abstract class MixinSubmitNodeCollection {

    @Unique
    private Object submitModelParameter_CU;

    @Inject(at = @At("HEAD"), method = "submitModel")
    private void onSubmitModelCaptureParameter(
            Model<?> model,
            Object parameter,
            PoseStack poseStack,
            RenderType renderType,
            int i,
            int j,
            int k,
            @Nullable TextureAtlasSprite textureAtlasSprite,
            int l,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
            CallbackInfo ci
    ) {
        submitModelParameter_CU = parameter;
    }

    @ModifyArg(
            method = "submitModel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$Storage;add(Lnet/minecraft/client/renderer/RenderType;Lnet/minecraft/client/renderer/SubmitNodeStorage$ModelSubmit;)V"),
            index = 1)
    private SubmitNodeStorage.ModelSubmit<?> onSubmitModelModifySubmission(SubmitNodeStorage.ModelSubmit<?> submission) {
        if (submitModelParameter_CU instanceof EntityRenderStateExtension state) {
            SubmitNodeStorageModelSubmitExtension extension = (SubmitNodeStorageModelSubmitExtension) (Object) submission;
            extension.setParameters_CU(state.getParameters_CU());
        }
        submitModelParameter_CU = null;
        return submission;
    }
}