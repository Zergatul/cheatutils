package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FastBreakConfig;
import com.zergatul.cheatutils.helpers.MixinMultiPlayerGameModeHelper;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode {

    @Shadow
    private int destroyDelay;

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

    @Inject(at = @At("HEAD"), method = "interactAt")
    private void onInteractAt(Player player, Entity entity, EntityHitResult hitResult, InteractionHand hand, CallbackInfoReturnable<InteractionResult> info) {
        Events.EntityInteract.trigger(entity);
    }

    @Inject(at = @At("HEAD"), method = "continueDestroyBlock")
    private void onContinueDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> info) {
        FastBreakConfig config = ConfigStore.instance.getConfig().fastBreakConfig;
        if (config.enabled && config.disableDestroyDelay) {
            this.destroyDelay = 0;
        }
    }
}