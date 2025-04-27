package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.GetFieldOfViewEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.esp.FreeCam;
import com.zergatul.cheatutils.render.MainFrameBuffer;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Inject(at = @At("HEAD"), method = "pick(F)V")
    private void onBeforePick(float vec33, CallbackInfo info) {
        FreeCam.instance.onBeforeGameRendererPick();
    }

    @Inject(at = @At("TAIL"), method = "pick(F)V")
    private void onAfterPick(float vec33, CallbackInfo info) {
        FreeCam.instance.onAfterGameRendererPick();
    }

    @Redirect(
            method = "renderItemInHand",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z", ordinal = 0))
    private boolean onRenderItemInHandIsFirstPerson(CameraType cameraType) {
        return FreeCam.instance.onRenderItemInHandIsFirstPerson(cameraType);
    }

    @Inject(
            at = @At(value = "RETURN", ordinal = 1),
            method = "getFov",
            cancellable = true)
    private void onGetFov(Camera camera, float partialTicks, boolean p_109144_, CallbackInfoReturnable<Float> info) {
        double fov = info.getReturnValue();
        GetFieldOfViewEvent event = new GetFieldOfViewEvent();
        event.set(fov);
        Events.GetFieldOfView.trigger(event);
        if (fov != event.get()) {
            info.setReturnValue((float) event.get());
        }
    }

    @Inject(at = @At("HEAD"), method = "render")
    private void onBeforeRender(DeltaTracker delta, boolean p_109096_, CallbackInfo info) {
        Events.RenderTickStart.trigger(delta);
    }

    @Inject(at = @At("HEAD"), method = "bobHurt(Lcom/mojang/blaze3d/vertex/PoseStack;F)V", cancellable = true)
    private void onBobHurt(PoseStack poseStack, float partialTicks, CallbackInfo info) {
        if (ConfigStore.instance.getConfig().bobHurtConfig.enabled) {
            info.cancel();
        }
    }

//    private GlyphFontRenderer fontRenderer;
//    private String text;
//
//    @Inject(at = @At("TAIL"), method = "render")
//    private void onAfterRender(DeltaTracker delta, boolean p_109096_, CallbackInfo info) {
//        Minecraft mc = Minecraft.getInstance();
//        if (!mc.isGameLoadFinished()) {
//            return;
//        }
//        if (fontRenderer == null) {
//            fontRenderer = new GlyphFontRenderer("Arial", 30);
//        }
//        if (text == null) {
//            text = "abcdefygjklmin === AWAVA";
//        }
//
//        //int scale = (int) mc.getWindow().getGuiScale(); // currently it is always integer
//        int scrWidth = mc.getWindow().getWidth();
//        int scrHeight = mc.getWindow().getHeight();
//        int halfScrWidth = scrWidth / 2;
//        int halfScrHeight = scrHeight / 2;
//
//        Matrix4f matrix = new Matrix4f();
//        matrix.ortho(-halfScrWidth, scrWidth - halfScrWidth, scrHeight - halfScrHeight, -halfScrHeight, -1, 1);
//
//        MainFrameBuffer.enter();
//        fontRenderer.drawText(matrix, text, -halfScrWidth + 100, -halfScrHeight + 100, Color.WHITE.getRGB());
//        MainFrameBuffer.exit();
//    }
}