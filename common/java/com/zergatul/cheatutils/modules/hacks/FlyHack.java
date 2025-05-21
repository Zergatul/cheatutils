package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FlyHackConfig;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.accessors.ServerboundMovePlayerPacketAccessor;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class FlyHack implements Module {

    public static final FlyHack instance = new FlyHack();

    private final Minecraft mc = Minecraft.getInstance();
    private int tickCounter;
    private ServerboundMovePlayerPacket antiKickPacket;

    private FlyHack() {
        NetworkPacketsController.instance.addClientPacketHandler(this::onClientPacket);
        Events.ClientTickEnd.add(this::onTickEnd);
    }

    private void onClientPacket(NetworkPacketsController.ClientPacketArgs args) {
        if (args.packet instanceof ServerboundMovePlayerPacket packet) {
            if (antiKickPacket != null) {
                if (args.packet != antiKickPacket) {
                    // don't send other movement packets for 1 tick
                    args.skip = true;
                    return;
                }
            }

            FlyHackConfig config = ConfigStore.instance.getConfig().flyHackConfig;
            if (config.enabled) {
                ((ServerboundMovePlayerPacketAccessor) packet).setOnGround_CU(config.onGroundFlag);
            }
        }
    }

    private void onTickEnd() {
        if (antiKickPacket != null) {
            // clear field on the next tick
            antiKickPacket = null;
        }

        FlyHackConfig config = ConfigStore.instance.getConfig().flyHackConfig;
        if (!config.enabled || !config.vanillaAntiFlyBypass) {
            tickCounter = 0;
            return;
        }

        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        tickCounter++;
        if (tickCounter >= config.antiFlyInterval) {
            tickCounter = 0;
            antiKickPacket = new ServerboundMovePlayerPacket.Pos(
                    player.getX(),
                    player.getY() - config.antiFlyDistance,
                    player.getZ(),
                    config.onGroundFlag,
                    false);
            NetworkPacketsController.instance.sendPacket(antiKickPacket);
        }
    }
}