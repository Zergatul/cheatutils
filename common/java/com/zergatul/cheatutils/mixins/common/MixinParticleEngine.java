package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class MixinParticleEngine {

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/particle/ParticleEngine;destroy(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V", cancellable = true)
    private void onDestroy(BlockPos p_107356_, BlockState p_107357_, CallbackInfo info) {
        if (ConfigStore.instance.getConfig().particlesConfig.disableBlockDestroyed) {
            info.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/particle/ParticleEngine;crack(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)V", cancellable = true)
    private void onCrack(BlockPos p_107368_, Direction p_107369_, CallbackInfo info) {
        if (ConfigStore.instance.getConfig().particlesConfig.disableBlockBreaking) {
            info.cancel();
        }
    }
}