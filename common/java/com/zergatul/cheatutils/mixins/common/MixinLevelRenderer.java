package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.extensions.LevelRendererExtension;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer implements LevelRendererExtension {

    // store projection matrix + bob views + portal distortions
    @Unique
    private Matrix4f modifiedProjectionMatrix_CU;

    public void setModifiedProjectionMatrix_CU(Matrix4f matrix) {
        this.modifiedProjectionMatrix_CU = matrix;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderLevelBegin(
            final GraphicsResourceAllocator resourceAllocator,
            final DeltaTracker deltaTracker,
            final boolean renderOutline,
            final CameraRenderState cameraState,
            final Matrix4fc modelViewMatrix,
            final GpuBufferSlice terrainFog,
            final Vector4f fogColor,
            final boolean shouldRenderSky,
            final CallbackInfo info
    ) {
        Events.BeforeRenderWorld.trigger();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelTargetBundle;clear()V"))
    private void onRenderLevelEnd(
            final GraphicsResourceAllocator resourceAllocator,
            final DeltaTracker deltaTracker,
            final boolean renderOutline,
            final CameraRenderState cameraState,
            final Matrix4fc modelViewMatrix,
            final GpuBufferSlice terrainFog,
            final Vector4f fogColor,
            final boolean shouldRenderSky,
            final CallbackInfo info
    ) {
        Events.AfterRenderWorld.trigger(new RenderWorldLastEvent(cameraState, this.modifiedProjectionMatrix_CU, deltaTracker));
        this.modifiedProjectionMatrix_CU = null;
    }
}