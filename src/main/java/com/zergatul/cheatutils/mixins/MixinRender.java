package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.modules.esp.EntityEsp;
import com.zergatul.cheatutils.render.EntityMaskRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Render.class)
public abstract class MixinRender {

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void onGetTeamColor(Entity entity, CallbackInfoReturnable<Integer> info) {
        if (EntityMaskRenderer.isRenderingMask()) {
            info.setReturnValue(0xFFFFFF);
            return;
        }
        Integer color = EntityEsp.INSTANCE.getOutlineColor(entity);
        if (color != null) {
            info.setReturnValue(color);
        }
    }

    @Inject(method = "renderLivingLabel", at = @At("HEAD"), cancellable = true)
    private void onRenderLabel(Entity entity, String text, double x, double y, double z, int maxDistance, CallbackInfo info) {
        if (EntityMaskRenderer.isRenderingMask()) {
            info.cancel();
        }
    }
}