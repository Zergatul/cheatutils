package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.modules.esp.FreeCam;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP {

    @Inject(at = @At("HEAD"), method = "getLook(F)Lnet/minecraft/util/math/Vec3d;", cancellable = true)
    private void onGetLook(float partialTicks, CallbackInfoReturnable<Vec3d> info) {
        FreeCam freeCam = FreeCam.instance;
        if (freeCam.shouldOverrideCameraEntityForPicking((EntityPlayerSP) (Object) this)) {
            info.setReturnValue(freeCam.getTargetLookVector());
        }
    }
}