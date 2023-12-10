package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.RenderWorldLayerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// moved into separate class to fight conflict with sodium
@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer2 {

    @Inject(
            method = "renderSectionLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack;DDDLorg/joml/Matrix4f;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;clearRenderState()V"),
            require = 0
    )
    private void onRenderChunkLayer(RenderType type, PoseStack poseStack, double p_172996_, double p_172997_, double p_172998_, Matrix4f projectionMatrix, CallbackInfo info) {
        if (type == RenderType.solid()) {
            Events.RenderSolidLayer.trigger(new RenderWorldLayerEvent(poseStack, projectionMatrix, Minecraft.getInstance().gameRenderer.getMainCamera()));
        }
    }
}