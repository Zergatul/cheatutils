package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.modules.esp.EntityEsp;
import com.zergatul.cheatutils.modules.esp.EspGlobal;
import com.zergatul.cheatutils.modules.esp.FreeCam;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.PlayerTurnByMouseEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Inject(at = @At("HEAD"), method = "turn(FF)V", cancellable = true)
    private void onTurn(float yaw, float pitch, CallbackInfo info) {
        if ((Object) this != Minecraft.getMinecraft().player) {
            return;
        }

        if (Events.PlayerTurnByMouse.trigger(new PlayerTurnByMouseEvent(-pitch, yaw))) {
            info.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "getPositionEyes(F)Lnet/minecraft/util/math/Vec3d;", cancellable = true)
    private void onGetPositionEyes(float partialTicks, CallbackInfoReturnable<Vec3d> info) {
        FreeCam freeCam = FreeCam.INSTANCE;
        if (freeCam.shouldOverrideCameraEntityForPicking((Entity) (Object) this)) {
            info.setReturnValue(new Vec3d(freeCam.getX(), freeCam.getY(), freeCam.getZ()));
        }
    }

    @Inject(at = @At("HEAD"), method = "getLook(F)Lnet/minecraft/util/math/Vec3d;", cancellable = true)
    private void onGetLook(float partialTicks, CallbackInfoReturnable<Vec3d> info) {
        FreeCam freeCam = FreeCam.INSTANCE;
        if (freeCam.shouldOverrideCameraEntityForPicking((Entity) (Object) this)) {
            info.setReturnValue(freeCam.getTargetLookVector());
        }
    }

    @Inject(at = @At("RETURN"), method = "getEntityBoundingBox()Lnet/minecraft/util/math/AxisAlignedBB;", cancellable = true)
    private void onGetEntityBoundingBox(CallbackInfoReturnable<AxisAlignedBB> info) {
        info.setReturnValue(FreeCam.INSTANCE.getTargetSearchBox((Entity) (Object) this, info.getReturnValue()));
    }

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void onIsGlowing(CallbackInfoReturnable<Boolean> info) {
        if (EntityEsp.INSTANCE.shouldEntityHaveOutline((Entity) (Object) this)) {
            info.setReturnValue(true);
        }
    }
}