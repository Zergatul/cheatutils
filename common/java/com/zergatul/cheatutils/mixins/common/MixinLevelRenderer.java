package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.extensions.EntityRenderStateExtension;
import com.zergatul.cheatutils.helpers.MixinLevelRendererHelper;
import com.zergatul.cheatutils.modules.esp.EntityEsp;
import com.zergatul.cheatutils.modules.esp.FreeCam;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
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
            Matrix4f projectionWithBob,
            Matrix4f projectionForCulling,
            GpuBufferSlice gpuBufferSlice,
            Vector4f vector4f,
            boolean b,
            CallbackInfo info
    ) {
        int program = GL20.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        Events.AfterRenderWorld.trigger(new RenderWorldLastEvent(camera, pose, projectionWithBob, delta));
        GL20.glUseProgram(program);
    }

    @Unique
    private Entity currentRenderedEntity_CU;

    @ModifyArg(
            method = "extractVisibleEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;extractEntity(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;"),
            index = 0)
    private Entity onStoreEntityBeforeCalculatingRenderState(Entity entity) {
        currentRenderedEntity_CU = entity;
        return entity;
    }

    @ModifyVariable(
            method = "extractVisibleEntities",
            at = @At(value = "STORE"),
            ordinal = 0)
    private EntityRenderState onModifyEntityRenderState(EntityRenderState state) {
        ((EntityRenderStateExtension) state).setParameters_CU(EntityEsp.instance.getEntityRenderParameters(currentRenderedEntity_CU));
        currentRenderedEntity_CU = null;
        return state;
    }

    @ModifyArg(
            method = "submitEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"),
            index = 0)
    private EntityRenderState onStoreCurrentEntitySubmit(EntityRenderState state) {
        MixinLevelRendererHelper.CURRENT_SUBMIT_ENTITY_RENDER_PARAMETERS = ((EntityRenderStateExtension) state).getParameters_CU();
        return state;
    }

    @Inject(
            method = "submitEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V", shift = At.Shift.AFTER))
    private void onAfterSubmittingEntityState(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector, CallbackInfo info) {
        MixinLevelRendererHelper.CURRENT_SUBMIT_ENTITY_RENDER_PARAMETERS = null;
    }
}