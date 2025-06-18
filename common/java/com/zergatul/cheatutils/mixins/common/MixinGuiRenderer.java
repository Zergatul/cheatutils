package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.zergatul.cheatutils.common.Events;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiRenderer.class)
public abstract class MixinGuiRenderer {

    @Shadow
    @Final
    private List<GuiRenderer.Draw> draws;

    @Inject(method = "draw", at = @At("HEAD"))
    private void onBeforeDraw(GpuBufferSlice gpuBufferSlice, CallbackInfo info) {
        if (this.draws.isEmpty()) {
            // never happens, even if turn off GUI with F1
            Events.BeforeBlurMainFramebuffer.trigger();
        }
    }

    /*@Inject(
            method = "draw",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DynamicUniforms;writeTransform(Lorg/joml/Matrix4fc;Lorg/joml/Vector4fc;Lorg/joml/Vector3fc;Lorg/joml/Matrix4fc;F)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;", shift = At.Shift.AFTER))
    private void onBeforeBlur(GpuBufferSlice gpuBufferSlice, CallbackInfo info) {
        Events.BeforeBlurMainFramebuffer.trigger();
    }*/

    @Inject(
            method = "draw",
            at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;executeDrawRange(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/pipeline/RenderTarget;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;II)V", ordinal = 0),
                    to = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/CommandEncoder;clearDepthTexture(Lcom/mojang/blaze3d/textures/GpuTexture;D)V")))
    private void onBeforeBlur(GpuBufferSlice gpuBufferSlice, CallbackInfo info) {
        Events.BeforeBlurMainFramebuffer.trigger();
    }
}