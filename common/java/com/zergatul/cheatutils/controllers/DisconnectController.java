package com.zergatul.cheatutils.controllers;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;

public class DisconnectController {

    public static final DisconnectController instance = new DisconnectController();

    private final Minecraft mc = Minecraft.getInstance();
    private String addText;

    public Component appendMessage(Component component) {
        if (addText == null) {
            return component;
        }

        if (component == null || component.getString().length() == 0) {
            component = MutableComponent.create(new LiteralContents(addText));
        } else {
            component = component.copy().append("\n").append(addText);
        }

        addText = null;

        return component;
    }

    public void disconnect(String message) {
        if (mc.player == null) {
            return;
        }

        addText = message;
        ClientboundDisconnectPacket packet = new ClientboundDisconnectPacket(MutableComponent.create(new LiteralContents("")));
        mc.player.connection.handleDisconnect(packet);
    }

    public void invalidChars(String message) {
        if (mc.player == null) {
            return;
        }

        addText = message;
        mc.player.connection.sendChat("\u00a7");
    }

    public void selfAttack(String message) {
        if (mc.player == null) {
            return;
        }

        addText = message;
        NetworkPacketsController.instance.sendPacket(ServerboundInteractPacket.createAttackPacket(mc.player, mc.player.isShiftKeyDown()));
    }
}