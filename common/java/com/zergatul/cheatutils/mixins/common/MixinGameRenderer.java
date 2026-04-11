package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.ResizeEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.extensions.LevelRendererExtension;
import com.zergatul.cheatutils.modules.esp.FreeCam;
import com.zergatul.cheatutils.modules.visuals.FullBright;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    private void onBeforeRender(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo info) {
        Events.RenderTickStart.trigger(deltaTracker);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;resize(II)V"))
    private void onResizeFramebuffers(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
        Events.FramebuffersResize.trigger(new ResizeEvent(this.gameRenderState.windowRenderState.width, this.gameRenderState.windowRenderState.height));
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderBuffers;endFrame()V"))
    private void onRenderBuffersEndFrame(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
        Events.RenderBuffersCleanUp.trigger();
    }

    @ModifyArg(
            method = "renderLevel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"))
    private Matrix4f onCaptureModifiedProjectionMatrix(Matrix4f matrix) {
        ((LevelRendererExtension) this.minecraft.levelRenderer).setModifiedProjectionMatrix_CU(matrix);
        return matrix;
    }

    @Inject(at = @At("HEAD"), method = "bobHurt", cancellable = true)
    private void onBobHurt(final CameraRenderState cameraState, final PoseStack poseStack, CallbackInfo info) {
        if (ConfigStore.instance.getConfig().bobHurtConfig.enabled) {
            info.cancel();
        }
    }

    @Inject(method = "extract", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LightmapRenderStateExtractor;extract(Lnet/minecraft/client/renderer/state/LightmapRenderState;F)V"))
    private void onBeforeLevelExtract(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
        FullBright.instance.shouldReturnNightVisionEffect = true;
    }

    @Inject(method = "extract", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/extract/LevelExtractor;extract(Lnet/minecraft/client/DeltaTracker;Lnet/minecraft/client/Camera;F)V", shift = At.Shift.AFTER))
    private void onAfterLevelExtract(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo info) {
        FullBright.instance.shouldReturnNightVisionEffect = false;
    }
}