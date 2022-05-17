package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.BoatHackConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Boat.class)
public class MixinBoat {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/entity/vehicle/Boat;getGroundFriction()F", cancellable = true)
    public void onGetGroundFriction(CallbackInfoReturnable<Float> info) {
        if (ConfigStore.instance.getConfig().boatHackConfig.overrideFriction) {
            Boat boat = (Boat) (Object) this;
            if (boat.level instanceof ClientLevel) {
                info.setReturnValue(ConfigStore.instance.getConfig().boatHackConfig.friction);
                info.cancel();
                return;
            }
        }
    }
}
