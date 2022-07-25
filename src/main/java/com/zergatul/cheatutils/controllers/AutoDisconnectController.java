package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AutoDisconnectController {

    public static final AutoDisconnectController instance = new AutoDisconnectController();

    private AutoDisconnectController() {

    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide && ConfigStore.instance.getConfig().autoDisconnectConfig.enabled) {
            if (event.getEntity() instanceof RemotePlayer) {
                var player = (RemotePlayer) event.getEntity();
                Component component = MutableComponent.create(new LiteralContents("AutoDisconnect module: " + player.getName().getString()));
                var packet = new ClientboundDisconnectPacket(component);
                Minecraft.getInstance().player.connection.handleDisconnect(packet);
            }
        }
    }
}
