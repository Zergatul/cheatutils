package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.modules.esp.FreeCam;
import com.zergatul.cheatutils.render.MainFrameBuffer;
import com.zergatul.cheatutils.render.gl.GlStateTracker;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.*;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer {

    @ModifyArg(
            method = "renderLevel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;cullTerrain(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;Z)V"),
            index = 2)
    private boolean onCallSetupRender(boolean isSpectator) {
        if (FreeCam.instance.isActive()) {
            return true;
        } else {
            return isSpectator;
        }
    }

    @Inject(at = @At("HEAD"), method = "renderLevel")
    private void onRenderLevelBegin(
            GraphicsResourceAllocator allocator,
            DeltaTracker delta,
            boolean renderBlockOutline,
            Camera camera,
            Matrix4f pose,
            Matrix4f projection,
            Matrix4f matrix,
            GpuBufferSlice gpuBufferSlice,
            Vector4f vector4f,
            boolean b,
            CallbackInfo info
    ) {
        Events.BeforeRenderWorld.trigger();
    }

    @Inject(at = @At("RETURN"), method = "renderLevel")
    private void onRenderLevelEnd(
            GraphicsResourceAllocator allocator,
            DeltaTracker delta,
            boolean renderBlockOutline,
            Camera camera,
            Matrix4f pose,
            Matrix4f projection,
            Matrix4f matrix,
            GpuBufferSlice gpuBufferSlice,
            Vector4f vector4f,
            boolean b,
            CallbackInfo info
    ) {
        GlStateTracker.save();
        MainFrameBuffer.enter();
        Events.AfterRenderWorld.trigger(new RenderWorldLastEvent(pose, projection, delta));
        MainFrameBuffer.exit();
        GlStateTracker.restore();
    }
}