package com.zergatul.cheatutils.mixins.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.ResizeEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.extensions.LevelRendererExtension;
import com.zergatul.cheatutils.modules.esp.FreeCam;
import com.zergatul.cheatutils.modules.visuals.FullBright;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private GameRenderState gameRenderState;

    @Redirect(
            method = "renderItemInHand",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z", ordinal = 0))
    private boolean onRenderItemInHandIsFirstPerson(CameraType cameraType) {
        return FreeCam.instance.onRenderItemInHandIsFirstPerson(cameraType);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onBeforeRender(CallbackInfo info) {
        Events.RenderTickStart.trigger(this.minecraft.getDeltaTracker());
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;resize(II)V"))
    private void onResizeFramebuffers(CallbackInfo info) {
        Events.FramebuffersResize.trigger(new ResizeEvent(this.gameRenderState.windowRenderState.width, this.gameRenderState.windowRenderState.height));
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderBuffers;endFrame()V"))
    private void onRenderBuffersEndFrame(CallbackInfo info) {
        Events.RenderBuffersCleanUp.trigger();
    }

    @ModifyArg(
            method = "renderLevel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;"))
    private Matrix4f onCaptureModifiedProjectionMatrix(Matrix4f matrix) {
        ((LevelRendererExtension) this.minecraft.levelRenderer).setModifiedProjectionMatrix_CU(matrix);
        return matrix;
    }

    @ModifyExpressionValue(
            method = "render3dHud",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"))
    private boolean onModifyIsFirstPerson3dCrosshair(boolean isFirstPerson) {
        // maybe need priority=2000 like in another call site
        return FreeCam.instance.onRenderCrosshairIsFirstPerson(isFirstPerson);
    }

    @Inject(at = @At("HEAD"), method = "bobHurt", cancellable = true)
    private void onBobHurt(final CameraRenderState cameraState, final PoseStack poseStack, CallbackInfo info) {
        if (ConfigStore.instance.getConfig().bobHurtConfig.enabled) {
            info.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "nightVisionScale", cancellable = true)
    private static void onBeforeNightVisionScale(LivingEntity camera, float a, CallbackInfoReturnable<Float> info) {
        if (FullBright.instance.shouldFakeNighVision()) {
            info.setReturnValue(1f);
        }
    }
}