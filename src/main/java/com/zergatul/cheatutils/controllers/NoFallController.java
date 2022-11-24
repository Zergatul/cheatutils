package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.interfaces.ServerboundMovePlayerPacketMixinInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class NoFallController {

    public static final NoFallController instance = new NoFallController();

    private final Minecraft mc = Minecraft.getInstance();

    private NoFallController() {
        NetworkPacketsController.instance.addClientPacketHandler(this::onClientPacket);
    }

    private void onClientPacket(NetworkPacketsController.ClientPacketArgs args) {
        if (args.packet instanceof ServerboundMovePlayerPacket packet) {
            if (mc.player.isFallFlying()) {
                // flying with elytra
                return;
            }

            var config = ConfigStore.instance.getConfig().noFallConfig;
            if (!config.enabled) {
                return;
            }

            if (mc.player.getDeltaMovement().y < -0.5) {
                ((ServerboundMovePlayerPacketMixinInterface) packet).setOnGround(true);
            }
        }
    }
}