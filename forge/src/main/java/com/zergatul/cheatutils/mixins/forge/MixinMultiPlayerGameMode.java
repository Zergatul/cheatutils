package com.zergatul.cheatutils.mixins.forge;

import com.zergatul.cheatutils.common.Events;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode {

    @Inject(
            method = "startDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V",
                    ordinal = 1),
            locals = LocalCapture.CAPTURE_FAILHARD,
            require = 1)
    private void onBeforeInstaMine(
            BlockPos pos,
            Direction direction,
            CallbackInfoReturnable<Boolean> info,
            PlayerInteractEvent.LeftClickBlock event,
            BlockState state
    ) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null &&
                event.getUseItem() != Event.Result.DENY &&
                !state.isAir() &&
                state.getDestroyProgress(mc.player, mc.player.level(), pos) >= 1.0F) {
            Events.BeforeInstaMine.trigger(pos);
        }
    }
}