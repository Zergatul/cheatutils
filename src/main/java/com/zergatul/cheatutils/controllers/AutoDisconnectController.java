package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AutoDisconnectController {

    public static final AutoDisconnectController instance = new AutoDisconnectController();

    private AutoDisconnectController() {

    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isClientSide && ConfigStore.instance.getConfig().autoDisconnectConfig.enabled) {
            if (event.getEntity() instanceof RemotePlayer) {
                var player = (RemotePlayer) event.getEntity();
                if (FreeCamController.instance.isActive() && FreeCamController.instance.getShadow() == player) {
                    return;
                }
                var packet = new ClientboundDisconnectPacket(new TextComponent("AutoDisconnect module: " + player.getName().getString()));
                Minecraft.getInstance().player.connection.handleDisconnect(packet);
            }
        }
    }
}
