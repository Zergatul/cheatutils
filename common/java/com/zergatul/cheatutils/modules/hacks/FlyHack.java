package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FlyHackConfig;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.extensions.ServerboundMovePlayerPacketExtension;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public class FlyHack implements Module {

    public static final FlyHack instance = new FlyHack();

    private final Minecraft mc = Minecraft.getInstance();
    private boolean abilitiesOverridden;
    private boolean oldFlying;
    private float oldFlyingSpeed;
    private int tickCounter;
    private ServerboundMovePlayerPacket antiKickPacket;

    private FlyHack() {
        NetworkPacketsController.instance.addClientPacketHandler(this::onClientPacket);
        Events.BeforePlayerAiStep.add(this::onBeforePlayerAiStep);
        Events.AfterPlayerAiStep.add(this::onAfterPlayerAiStep);
        Events.InGameTickEnd.add(this::onTickEnd);
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
                ((ServerboundMovePlayerPacketExtension) packet).setOnGround_CU(config.onGroundFlag);
            }
        }
    }

    private void onBeforePlayerAiStep() {
        assert mc.player != null;

        FlyHackConfig config = ConfigStore.instance.getConfig().flyHackConfig;
        if (config.enabled) {
            LocalPlayer player = mc.player;

            oldFlying = player.getAbilities().flying;
            oldFlyingSpeed = player.getAbilities().getFlyingSpeed();

            player.getAbilities().flying = true;
            if (config.overrideFlyingSpeed) {
                player.getAbilities().setFlyingSpeed(config.flyingSpeed);
            }

            abilitiesOverridden = true;
        }
    }

    private void onAfterPlayerAiStep() {
        assert mc.player != null;

        if (abilitiesOverridden) {
            LocalPlayer player = mc.player;
            player.getAbilities().flying = oldFlying;
            player.getAbilities().setFlyingSpeed(oldFlyingSpeed);
            abilitiesOverridden = false;
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