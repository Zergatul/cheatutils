package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FastBreakConfig;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class MixinAbstractBlockAbstractBlockState {

    @Inject(at = @At("RETURN"), method = "Lnet/minecraft/block/AbstractBlock$AbstractBlockState;calcBlockBreakingDelta(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F", cancellable = true)
    private void onGetDestroyProgress(PlayerEntity player, BlockView level, BlockPos pos, CallbackInfoReturnable<Float> info) {
        if (player.world.isClient()) {
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