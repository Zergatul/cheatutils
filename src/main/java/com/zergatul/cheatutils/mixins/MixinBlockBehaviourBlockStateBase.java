package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FastBreakConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class MixinBlockBehaviourBlockStateBase {

    @Inject(at = @At("RETURN"), method = "Lnet/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase;getDestroyProgress(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F", cancellable = true)
    private void onGetDestroyProgress(Player player, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> info) {
        if (player.level.isClientSide()) {
            FastBreakConfig config = ConfigStore.instance.getConfig().fastBreakConfig;
            if (config.enabled) {
                float oldValue = info.getReturnValue();
                float newValue = (float) (oldValue * config.factor);
                if (oldValue < 1 && newValue >= 1) {
                    // we cannot make block instamineable
                    info.setReturnValue(0.999999f);
                } else {
                    info.setReturnValue(newValue);
                }
            }
        }
    }
}