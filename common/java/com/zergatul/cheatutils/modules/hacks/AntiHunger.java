package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.accessors.ServerboundMovePlayerPacketAccessor;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class AntiHunger implements Module {

    public static final AntiHunger instance = new AntiHunger();

    private AntiHunger() {
        NetworkPacketsController.instance.addClientPacketHandler(this::onClientPacket);
    }

    private void onClientPacket(NetworkPacketsController.ClientPacketArgs args) {
        if (args.packet instanceof ServerboundMovePlayerPacket packet) {
            if (!ConfigStore.instance.getConfig().antiHungerConfig.enabled) {
                return;
            }
            if (NoFall.instance.isActive()) {
                return;
            }
            if (ConfigStore.instance.getConfig().flyHackConfig.enabled) {
                return;
            }
            ((ServerboundMovePlayerPacketAccessor) packet).setOnGround_CU(false);
        }
    }
}