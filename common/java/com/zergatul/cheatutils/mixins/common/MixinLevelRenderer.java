package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.extensions.LevelRendererExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Matrix4f;
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

    @Unique
    private Matrix4f storedModelViewMatrix_CU;

    public void setModifiedProjectionMatrix_CU(Matrix4f matrix) {
        this.modifiedProjectionMatrix_CU = matrix;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderLevelBegin(
            final GraphicsResourceAllocator resourceAllocator,
            final boolean renderOutline,
            final CameraRenderState cameraState,
            final GpuBufferSlice terrainFog,
            final Vector4f fogColor,
            final boolean shouldRenderSky,
            final boolean consistentDepthRequired,
            final CallbackInfo info
    ) {
        Events.BeforeRenderWorld.trigger();
    }

    @Inject(
            method = "render",
            at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4fStack;popMatrix()Lorg/joml/Matrix4fStack;"))
    private void onRenderLevelRememberLastModelViewMatrix(
            final GraphicsResourceAllocator resourceAllocator,
            final boolean renderOutline,
            final CameraRenderState cameraState,
            final GpuBufferSlice terrainFog,
            final Vector4f fogColor,
            final boolean shouldRenderSky,
            final boolean consistentDepthRequired,
            final CallbackInfo info
    ) {
        // fix for Iris compatibility
        this.storedModelViewMatrix_CU = new Matrix4f(RenderSystem.getModelViewStack());
    }

    @Inject(
            method = "render",
            at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4fStack;popMatrix()Lorg/joml/Matrix4fStack;", shift = At.Shift.AFTER))
    private void onRenderLevelEnd(
            final GraphicsResourceAllocator resourceAllocator,
            final boolean renderOutline,
            final CameraRenderState cameraState,
            final GpuBufferSlice terrainFog,
            final Vector4f fogColor,
            final boolean shouldRenderSky,
            final boolean consistentDepthRequired,
            final CallbackInfo info
    ) {
        ProfilerFiller profiler = Profiler.get();
        profiler.push(Constants.MOD_ID + " : AfterRenderWorld");
        // fix for Iris compatibility
        RenderSystem.getModelViewStack().pushMatrix();
        RenderSystem.getModelViewStack().mul(this.storedModelViewMatrix_CU);
        Events.AfterRenderWorld.trigger(new RenderWorldLastEvent(cameraState, this.modifiedProjectionMatrix_CU, Minecraft.getInstance().getDeltaTracker()));
        RenderSystem.getModelViewStack().popMatrix();
        profiler.pop();

        this.modifiedProjectionMatrix_CU = null;
        this.storedModelViewMatrix_CU = null;
    }
}