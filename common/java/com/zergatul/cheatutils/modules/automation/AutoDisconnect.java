package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;

public class AutoDisconnect implements Module {

    public static final AutoDisconnect instance = new AutoDisconnect();

    private Runnable script;

    private AutoDisconnect() {
        Events.ClientTickEnd.add(this::onClientTickEnd);
        //Events.PlayerAdded.add(this::onPlayerAdded);
    }

    /*private void onPlayerAdded(AbstractClientPlayer player) {
        if (ConfigStore.instance.getConfig().autoDisconnectConfig.enabled) {
            if (player instanceof RemotePlayer) {
                Component component = MutableComponent.create(new LiteralContents("AutoDisconnect module: " + player.getName().getString()));
                var packet = new ClientboundDisconnectPacket(component);
                Minecraft.getInstance().player.connection.handleDisconnect(packet);
            }
        }
    }*/

    public void setScript(Runnable script) {
        this.script = script;
    }

    private void onClientTickEnd() {
        if (!ConfigStore.instance.getConfig().autoDisconnectConfig.enabled) {
            return;
        }
        if (script != null) {
            script.run();
        }
    }
}