package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.modules.esp.FreeCam;
import net.minecraft.client.renderer.EntityRenderer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.FloatBuffer;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Unique
    private final FloatBuffer matrixBuffer_CU = BufferUtils.createFloatBuffer(16);

    @Unique
    private RenderWorldLastEvent renderWorldLastEvent_CU;

    @Inject(
            method = "renderWorldPass(IFJ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/client/ForgeHooksClient;dispatchRenderLast(Lnet/minecraft/client/renderer/RenderGlobal;F)V",
                    remap = false))
    private void onCaptureWorldMatrices(int pass, float partialTicks, long finishTimeNano, CallbackInfo info) {
        // Capture before hand rendering replaces the world projection matrix.
        matrixBuffer_CU.clear();
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, matrixBuffer_CU);
        Matrix4f projection = new Matrix4f();
        projection.load(matrixBuffer_CU);
        matrixBuffer_CU.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, matrixBuffer_CU);
        Matrix4f modelView = new Matrix4f();
        modelView.load(matrixBuffer_CU);
        renderWorldLastEvent_CU = new RenderWorldLastEvent(partialTicks, Matrix4f.mul(projection, modelView, null));
    }

    @Inject(
            method = "updateCameraAndRender(FJ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/EntityRenderer;renderWorld(FJ)V"))
    private void onBeforeUpdateCameraAndRender(float partialTicks, long nanoTime, CallbackInfo info) {
        Events.BeforeRenderWorld.trigger();
    }

    @Inject(
            method = "updateCameraAndRender(FJ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/EntityRenderer;renderWorld(FJ)V",
                    shift = At.Shift.AFTER))
    private void onAfterUpdateCameraAndRender(float partialTicks, long nanoTime, CallbackInfo info) {
        Events.AfterRenderWorld.trigger(renderWorldLastEvent_CU);
    }

    @Inject(at = @At("HEAD"), method = "getMouseOver(F)V")
    private void onBeforeGetMouseOver(float partialTicks, CallbackInfo info) {
        Events.OnBeforePick.trigger();
    }

    @Inject(at = @At("RETURN"), method = "getMouseOver(F)V")
    private void onAfterGetMouseOver(float partialTicks, CallbackInfo info) {
        Events.OnAfterPick.trigger();
    }

    @Inject(at = @At("HEAD"), method = "applyBobbing(F)V", cancellable = true)
    private void onApplyBobbing(float partialTicks, CallbackInfo info) {
        if (FreeCam.INSTANCE.shouldDisableBobbing()) {
            info.cancel();
        }
    }
}