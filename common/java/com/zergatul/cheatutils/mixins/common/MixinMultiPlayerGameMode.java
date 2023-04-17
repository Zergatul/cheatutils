package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.helpers.MixinMultiPlayerGameModeHelper;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode {

    @Inject(at = @At("HEAD"), method = "releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V", cancellable = true)
    private void onReleaseUsingItem(Player p_105278_, CallbackInfo info) {
        if (MixinMultiPlayerGameModeHelper.disableReleaseUsingItem) {
            info.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "useItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;", cancellable = true)
    private void onUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> info) {
        if (ConfigStore.instance.getConfig().antiRespawnResetConfig.enabled) {
            BlockState state = player.clientLevel.getBlockState(hitResult.getBlockPos());
            if (state.getBlock() instanceof BedBlock) {
                info.setReturnValue(InteractionResult.FAIL);
                return;
            }
            if (state.getBlock() == Blocks.RESPAWN_ANCHOR) {
                info.setReturnValue(InteractionResult.FAIL);
            }
        }
    }
}