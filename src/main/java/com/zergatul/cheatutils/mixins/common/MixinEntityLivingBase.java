package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.modules.esp.FreeCam;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase {

    @Inject(at = @At("HEAD"), method = "getLook(F)Lnet/minecraft/util/math/Vec3d;", cancellable = true)
    private void onGetLook(float partialTicks, CallbackInfoReturnable<Vec3d> info) {
        FreeCam freeCam = FreeCam.instance;
        if (freeCam.shouldOverrideCameraEntityForPicking((EntityLivingBase) (Object) this)) {
            info.setReturnValue(freeCam.getTargetLookVector());
        }
    }
}