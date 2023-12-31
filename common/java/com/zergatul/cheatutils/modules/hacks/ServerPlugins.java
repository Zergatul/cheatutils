package com.zergatul.cheatutils.modules.hacks;

import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ServerPluginsConfig;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class ServerPlugins implements Module {

    public static final ServerPlugins instance = new ServerPlugins();

    private final Minecraft mc = Minecraft.getInstance();
    //private final String completion = "/:abcdefghijklmnopqrstuvwxyz0123456789-";
    private long logInNano;
    private State state;
    private int suggestionId;
    private String[] plugins;
    private State bukkitState;
    private int bukkitSuggestionId;
    private String[] bukkitPlugins;
    private Random random = new Random();
    private Logger logger = LogManager.getLogger(ServerPlugins.class);
    private int delay = 20;
    private int waitResponseDelay = 4;

    private ServerPlugins() {
        Events.ClientPlayerLoggingIn.add(this::onLogIn);
        Events.ClientPlayerLoggingOut.add(this::onLogOut);
        NetworkPacketsController.instance.addServerPacketHandler(this::onServerPacket);

        reset();
    }

    public String[] getPlugins() {
        if (plugins == null) {
            scanPlugins();
            return new String[0];
        } else {
            return plugins.clone();
        }
    }

    public String[] getBukkitPlugins() {
        if (bukkitPlugins == null) {
            scanBukkitPlugins();
            return new String[0];
        } else {
            return bukkitPlugins.clone();
        }
    }

    public void reset() {
        logInNano = 0;
        state = State.INIT;
        bukkitState = State.INIT;
        plugins = null;
        bukkitPlugins = null;
    }

    private void scanPlugins() {
        if (mc.level == null) {
            return;
        }

        if (logInNano == 0) {
            return;
        }

        if (state != State.INIT) {
            return;
        }

        ServerPluginsConfig config = ConfigStore.instance.getConfig().serverPluginsConfig;
        if (System.nanoTime() - logInNano < (long) config.waitTicks * 50000000) {
            return;
        }

        state = State.SENT_PACKET;
        suggestionId = random.nextInt(1000000000);
        NetworkPacketsController.instance.sendPacket(new ServerboundCommandSuggestionPacket(suggestionId, "/"));
    }

    private void scanBukkitPlugins() {
        if (mc.level == null) {
            return;
        }

        if (logInNano == 0) {
            return;
        }

        if (bukkitState != State.INIT) {
            return;
        }

        ServerPluginsConfig config = ConfigStore.instance.getConfig().serverPluginsConfig;
        if (System.nanoTime() - logInNano < (long) config.waitTicks * 50000000) {
            return;
        }

        bukkitState = State.SENT_PACKET;
        bukkitSuggestionId = random.nextInt(1000000000);
        NetworkPacketsController.instance.sendPacket(new ServerboundCommandSuggestionPacket(bukkitSuggestionId, "bukkit:ver "));
    }

    private void onLogIn(Connection connection) {
        logInNano = System.nanoTime();
    }

    private void onLogOut() {
        reset();
    }

    private void onServerPacket(NetworkPacketsController.ServerPacketArgs args) {
        if (args.packet instanceof ClientboundCommandSuggestionsPacket packet) {
            if (state == State.SENT_PACKET && suggestionId == packet.getId()) {
                state = State.RECEIVED_PACKET;
                Suggestions suggestions = packet.getSuggestions();
                plugins = suggestions.getList().stream().map(Suggestion::getText).toArray(String[]::new);
                if (ConfigStore.instance.getConfig().serverPluginsConfig.autoPrint) {
                    for (String plugin: plugins) {
                        mc.getChatListener().handleSystemMessage(MutableComponent.create(new PlainTextContents.LiteralContents(plugin)), false);
                    }
                }
            }

            if (bukkitState == State.SENT_PACKET && bukkitSuggestionId == packet.getId()) {
                bukkitState = State.RECEIVED_PACKET;
                Suggestions suggestions = packet.getSuggestions();
                bukkitPlugins = suggestions.getList().stream().map(Suggestion::getText).toArray(String[]::new);
                if (ConfigStore.instance.getConfig().serverPluginsConfig.autoPrint) {
                    for (String plugin: bukkitPlugins) {
                        mc.getChatListener().handleSystemMessage(MutableComponent.create(new PlainTextContents.LiteralContents(plugin)), false);
                    }
                }
            }
        }
    }

    private enum State {
        INIT,
        SENT_PACKET,
        RECEIVED_PACKET
    }
}