package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.ModMain;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerPacketListenerTemp {

    @Shadow
    public ServerPlayer player;

    @Shadow @Final private static Logger LOGGER;

    @Inject(at = @At("HEAD"), method = "handleMovePlayer")
    public void onBeforeHandleMovePlayer(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        if (packet.hasRotation()) {
            LOGGER.info("Tick#{} Incoming packet.xRot={} packet.yRot={} player.xRot={} player.yRot={} yHeadRot={}",
                    player.level().getGameTime(),
                    String.format("%.3f", packet.getXRot(0)),
                    String.format("%.3f", packet.getYRot(0)),
                    String.format("%.3f", player.getXRot()),
                    String.format("%.3f", player.getYRot()),
                    String.format("%.3f", player.getYHeadRot()));
        }
    }

    @Inject(at = @At("TAIL"), method = "handleMovePlayer")
    public void onAfterHandleMovePlayer(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        /*if (packet.hasRotation()) {
            logger.info("After: player.xRot={} player.yRot={} yHeadRot={}",
                    String.format("%.3f", player.getXRot()),
                    String.format("%.3f", player.getYRot()),
                    String.format("%.3f", player.getYHeadRot()));
        }*/
    }

    @Inject(at = @At("HEAD"), method = "handleUseItemOn")
    private void onBeforeHandleUseItemOn(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
        LOGGER.info("Tick#{} Use item on: player.xRot={} player.yRot={} yHeadRot={}",
                player.level().getGameTime(),
                String.format("%.3f", player.getXRot()),
                String.format("%.3f", player.getYRot()),
                String.format("%.3f", player.getYHeadRot()));
    }
}