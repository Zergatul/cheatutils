package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.PigHackConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Pig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Pig.class)
public class MixinPig {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/entity/animal/Pig;canBeControlledBy(Lnet/minecraft/world/entity/Entity;)Z", cancellable = true)
    public void onCanBeControlledBy(Entity entity, CallbackInfoReturnable<Boolean> info) {
        PigHackConfig config = ConfigStore.instance.getConfig().pigHackConfig;
        if (config.enabled && config.allowRideWithoutCarrot) {
            var pig = (Pig) (Object) this;
            if (pig.level.isClientSide) {
                info.setReturnValue(true);
                info.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/entity/animal/Pig;getSteeringSpeed()F", cancellable = true)
    public void onGetSteeringSpeed(CallbackInfoReturnable<Float> info) {
        PigHackConfig config = ConfigStore.instance.getConfig().pigHackConfig;
        if (config.enabled && config.overrideSteeringSpeed) {
            var pig = (Pig) (Object) this;
            if (pig.level.isClientSide) {
                info.setReturnValue(config.steeringSpeed);
                info.cancel();
            }
        }
    }
}
