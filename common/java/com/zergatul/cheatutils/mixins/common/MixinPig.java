package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.PigHackConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Pig.class)
public abstract class MixinPig {

    @Inject(at = @At("HEAD"), method = "getControllingPassenger()Lnet/minecraft/world/entity/LivingEntity;", cancellable = true)
    private void onGetControllnigPassenger(CallbackInfoReturnable<LivingEntity> info) {
        PigHackConfig config = ConfigStore.instance.getConfig().pigHackConfig;
        if (config.enabled && config.allowRideWithoutCarrot) {
            var pig = (Pig) (Object) this;
            if (pig.level().isClientSide) {
                if (pig.isSaddled()) {
                    Entity entity = pig.getFirstPassenger();
                    if (entity instanceof Player player) {
                        info.setReturnValue(player);
                    }
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "getRiddenSpeed(Lnet/minecraft/world/entity/player/Player;)F", cancellable = true)
    private void onGetRiddenSpeed(Player player, CallbackInfoReturnable<Float> info) {
        PigHackConfig config = ConfigStore.instance.getConfig().pigHackConfig;
        if (config.enabled && config.overrideSteeringSpeed) {
            var pig = (Pig) (Object) this;
            if (pig.level().isClientSide) {
                info.setReturnValue(config.steeringSpeed);
                info.cancel();
            }
        }
    }
}