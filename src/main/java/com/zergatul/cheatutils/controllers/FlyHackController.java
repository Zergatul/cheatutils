package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FlyHackConfig;
import com.zergatul.cheatutils.interfaces.ServerboundMovePlayerPacketMixinInterface;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class FlyHackController {

    public static final FlyHackController instance = new FlyHackController();

    private FlyHackController() {
        NetworkPacketsController.instance.addClientPacketHandler(this::onClientPacket);
    }

    private void onClientPacket(NetworkPacketsController.ClientPacketArgs args) {
        if (args.packet instanceof ServerboundMovePlayerPacket packet) {
            FlyHackConfig config = ConfigStore.instance.getConfig().flyHackConfig;
            if (config.enabled) {
                ((ServerboundMovePlayerPacketMixinInterface) packet).setOnGround(config.onGroundFlag);
            }
        }
    }
}