package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.common.Events;


import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderManager.class)
public abstract class MixinRenderManager {

    @Inject(at = @At("HEAD"), method = "renderEntityStatic(Lnet/minecraft/entity/Entity;FZ)V")
    private void onBeforeRenderEntitySimple(Entity entity, float partialTicks, boolean b, CallbackInfo info) {
        Events.BeforeRenderEntity.trigger(entity);
    }

    @Inject(at = @At("TAIL"), method = "renderEntityStatic(Lnet/minecraft/entity/Entity;FZ)V")
    private void onAfterRenderEntitySimple(Entity entity, float partialTicks, boolean b, CallbackInfo info) {
        Events.AfterRenderEntity.trigger(entity);
    }
}