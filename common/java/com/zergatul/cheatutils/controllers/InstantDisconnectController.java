package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.helpers.MixinClientPacketListenerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;

import java.util.Locale;

public class InstantDisconnectController {

    public static final InstantDisconnectController instance = new InstantDisconnectController();

    private final Minecraft mc = Minecraft.getInstance();

    private InstantDisconnectController() {

    }

    public void onClientTickEnd() {
        if (!ConfigStore.instance.getConfig().instantDisconnectConfig.enabled) {
            return;
        }
        if (mc.player != null && mc.level != null) {
            MixinClientPacketListenerHelper.appendDisconnectMessage = String.format(Locale.ROOT, "Disconnect XYZ: %.3f / %.5f / %.3f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
            // self-attack to instant disconnect
            NetworkPacketsController.instance.sendPacket(ServerboundInteractPacket.createAttackPacket(mc.player, mc.player.isShiftKeyDown()));
        }
    }
}