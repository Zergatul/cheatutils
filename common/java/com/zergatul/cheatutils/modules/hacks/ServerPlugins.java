package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.ServerPluginsConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.utils.PacketUtils;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;

import java.util.List;
import java.util.Random;

public class ServerPlugins implements Module {

    public static final ServerPlugins INSTANCE = new ServerPlugins();

    private final SettingHandle<ServerPluginsConfig> settings = Settings.get(ServerPluginsConfig.class);
    private final Minecraft mc = Minecraft.getInstance();
    private final Random random = new Random();
    //private final String completion = "/:abcdefghijklmnopqrstuvwxyz0123456789-";
    private long logInNano;
    private State state;
    private int suggestionId;
    private String[] plugins;
    private State bukkitState;
    private int bukkitSuggestionId;
    private String[] bukkitPlugins;

    private ServerPlugins() {
        Events.ClientPlayerLoggingIn.add(this::onLogIn);
        Events.BeforeHandlePacket.add(this::onServerPacket);
        Events.ClientPlayerLoggingOut.add(this::onLogOut);

        WebApiRegistry.INSTANCE.register("server-plugins", settings, ServerPluginsConfig.class);

        reset();
    }

    public SettingHandle<ServerPluginsConfig> getSettings() {
        return settings;
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
        if (mc.player == null) {
            return;
        }

        if (logInNano == 0) {
            return;
        }

        if (state != State.INIT) {
            return;
        }

        ServerPluginsConfig config = settings.get();
        if (System.nanoTime() - logInNano < (long) config.waitTicks() * 50000000) {
            return;
        }

        state = State.SENT_PACKET;
        suggestionId = random.nextInt(1000000000);
        PacketUtils.send(mc.player, new ServerboundCommandSuggestionPacket(suggestionId, "/"));
    }

    private void scanBukkitPlugins() {
        if (mc.player == null) {
            return;
        }

        if (logInNano == 0) {
            return;
        }

        if (bukkitState != State.INIT) {
            return;
        }

        ServerPluginsConfig config = settings.get();
        if (System.nanoTime() - logInNano < (long) config.waitTicks() * 50000000) {
            return;
        }

        bukkitState = State.SENT_PACKET;
        bukkitSuggestionId = random.nextInt(1000000000);
        PacketUtils.send(mc.player, new ServerboundCommandSuggestionPacket(bukkitSuggestionId, "bukkit:ver "));
    }

    private void onLogIn(Connection connection) {
        logInNano = System.nanoTime();
    }

    private void onLogOut() {
        reset();
    }

    private boolean onServerPacket(Packet<?> packet) {
        if (packet instanceof ClientboundCommandSuggestionsPacket suggestionsPacket) {
            if (state == State.SENT_PACKET && suggestionId == suggestionsPacket.id()) {
                state = State.RECEIVED_PACKET;
                List<ClientboundCommandSuggestionsPacket.Entry> suggestions = suggestionsPacket.suggestions();
                plugins = suggestions.stream().map(ClientboundCommandSuggestionsPacket.Entry::text).toArray(String[]::new);
                if (settings.get().autoPrint()) {
                    for (String plugin: plugins) {
                        mc.gui.chatListener().handleSystemMessage(MutableComponent.create(new PlainTextContents.LiteralContents(plugin)), false);
                    }
                }
            }

            if (bukkitState == State.SENT_PACKET && bukkitSuggestionId == suggestionsPacket.id()) {
                bukkitState = State.RECEIVED_PACKET;
                List<ClientboundCommandSuggestionsPacket.Entry> suggestions = suggestionsPacket.suggestions();
                bukkitPlugins = suggestions.stream().map(ClientboundCommandSuggestionsPacket.Entry::text).toArray(String[]::new);
                if (settings.get().autoPrint()) {
                    for (String plugin: bukkitPlugins) {
                        mc.gui.chatListener().handleSystemMessage(MutableComponent.create(new PlainTextContents.LiteralContents(plugin)), false);
                    }
                }
            }
        }
        return true;
    }

    private enum State {
        INIT,
        SENT_PACKET,
        RECEIVED_PACKET
    }
}