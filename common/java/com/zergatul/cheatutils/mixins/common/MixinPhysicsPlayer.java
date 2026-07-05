package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.PhysicsConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class MixinPhysicsPlayer {

    @ModifyVariable(
            method = "travelInAir",
            at = @At(value = "STORE", ordinal = 0)
    )
    private float modifyBlockFriction(float original) {
        PhysicsConfig config = ConfigStore.instance.getConfig().physicsConfig;

        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof Player && config.overridePlayerFriction) {
            return (float) config.playerFriction;
        }

        return original;
    }
}