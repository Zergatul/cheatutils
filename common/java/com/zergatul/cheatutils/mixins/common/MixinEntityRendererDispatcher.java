package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.helpers.MixinLevelRendererHelper;
import com.zergatul.cheatutils.modules.esp.EntityEsp;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityRenderDispatcher.class)
public abstract class MixinEntityRendererDispatcher {

    @ModifyVariable(
            method = "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/EntityRenderer;)V",
            at = @At("HEAD"), argsOnly = true)
    private MultiBufferSource onRenderModifyBufferSource(MultiBufferSource bufferSource) {
        Entity entity = MixinLevelRendererHelper.current;
        if (entity != null) {
            return EntityEsp.instance.onRenderEntityModifyBufferSource(entity, bufferSource);
        } else {
            return bufferSource;
        }
    }
}