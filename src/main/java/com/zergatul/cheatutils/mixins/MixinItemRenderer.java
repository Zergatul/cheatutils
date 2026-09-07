package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.modules.esp.FreeCam;
import net.minecraft.client.renderer.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Inject(at = @At("HEAD"), method = "rotateArm", cancellable = true)
    private void onRotateArm(float partialTicks, CallbackInfo info) {
        if (FreeCam.INSTANCE.shouldRenderHands() && FreeCam.INSTANCE.isActive()) {
            info.cancel();
        }
    }
}