package com.zergatul.cheatutils.mixins.forge;

import com.zergatul.cheatutils.common.Events;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinMultiPlayerGameMode {

    @Inject(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;destroyBlock(Lnet/minecraft/core/BlockPos;)Z"),
            method = "lambda$startDestroyBlock$1")
    private void onBeforeInstaMine(BlockState blockState, PlayerInteractEvent.LeftClickBlock event, BlockPos pos, Direction direction, int p_233728_, CallbackInfoReturnable<Packet<?>> cir) {
        Events.BeforeInstaMine.trigger(pos);
    }
}