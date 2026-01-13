package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.BeforeAttackEvent;
import com.zergatul.cheatutils.common.events.PlayerReleaseUsingItemEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FastBreakConfig;
import com.zergatul.cheatutils.extensions.MultiPlayerGameModeExtension;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode implements MultiPlayerGameModeExtension {

    @Shadow
    private int destroyDelay;


    @Shadow
    private GameType localPlayerMode;

    @Shadow
    protected abstract void ensureHasSentCarriedItem();

    @Shadow
    @Final
    private ClientPacketListener connection;

    /**
     * A vanilla implementation of the
     * {@code void attack(Player player, Entity entity)}
     * method.<br>
     * This method will NOT trigger automations / events from the regular method.<br>
     * intended to only be used internally to make modules that trigger on beforeAttackMethod
     * Should be used in this format:<br>
     *  {@code ((MultiPlayerGameModeExtension) mc.gameMode).attackClone(mc.player, entity);}
     *  <br><br>
     *  <B>If you see this comment, you are not using the correct syntax. Make sure to cast the call first!</B>
     **/
    public void attackClone_CU(Player player, Entity entity) {
        this.ensureHasSentCarriedItem();

        this.connection.send(ServerboundInteractPacket.createAttackPacket(entity, player.isShiftKeyDown()));
        if (this.localPlayerMode != GameType.SPECTATOR) {
            player.attack(entity);
            player.resetAttackStrengthTicker();
        }
    }

    @Inject(at = @At("HEAD"), method = "releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V", cancellable = true)
    private void onReleaseUsingItem(Player player, CallbackInfo info) {
        if (Events.PlayerReleaseUsingItem.trigger(new PlayerReleaseUsingItemEvent())) {
            info.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "useItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;", cancellable = true)
    private void onUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> info) {
        if (ConfigStore.instance.getConfig().antiRespawnResetConfig.enabled) {
            BlockState state = player.level().getBlockState(hitResult.getBlockPos());
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

    @Inject(at = @At("HEAD"), method = "startDestroyBlock")
    private void onBeforeStartDestroyBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> info) {
        Events.StartDestroyBlock.trigger(blockPos);
    }

    @Inject(at = @At("HEAD"), method = "continueDestroyBlock")
    private void onContinueDestroyBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> info) {
        FastBreakConfig config = ConfigStore.instance.getConfig().fastBreakConfig;
        if (config.enabled && config.disableDestroyDelay) {
            this.destroyDelay = 0;
        }
    }

    @Inject(at = @At("HEAD"), method = "attack", cancellable = true)
    private void onAttack(Player player, Entity entity, CallbackInfo ci) {
        if (Events.BeforeAttack.trigger(new BeforeAttackEvent())) {
            ci.cancel();
        }
    }

    @Inject(at = @At("TAIL"), method = "attack", cancellable = false)
    private void afterAttack(Player player, Entity entity, CallbackInfo ci) {
        Events.AfterAttack.trigger();
    }
}

