package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.modules.esp.EntityEsp;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Render.class)
public abstract class MixinRender {

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void onGetTeamColor(Entity entity, CallbackInfoReturnable<Integer> info) {
        Integer color = EntityEsp.INSTANCE.getOutlineColor(entity);
        if (color != null) {
            info.setReturnValue(color);
        }
    }
}