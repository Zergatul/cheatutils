package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.interfaces.ServerboundMovePlayerPacketMixinInterface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFallController {

    public static final NoFallController instance = new NoFallController();

    private final MinecraftClient mc = MinecraftClient.getInstance();

    private NoFallController() {
        NetworkPacketsController.instance.addClientPacketHandler(this::onClientPacket);
    }

    private void onClientPacket(NetworkPacketsController.ClientPacketArgs args) {
        if (args.packet instanceof PlayerMoveC2SPacket packet) {
            if (mc.player.isFallFlying()) {
                // flying with elytra
                return;
            }

            var config = ConfigStore.instance.getConfig().noFallConfig;
            if (!config.enabled) {
                return;
            }

            if (mc.player.getVelocity().y < -0.5) {
                ((ServerboundMovePlayerPacketMixinInterface) packet).setOnGround(true);
            }
        }
    }
}