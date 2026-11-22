package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.utils.PacketUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;

import java.util.ArrayList;
import java.util.List;

public class FakeLag implements Module {

    public static final FakeLag INSTANCE = new FakeLag();

    private final Minecraft mc = Minecraft.getInstance();
    private final List<Packet<?>> clientPackets = new ArrayList<>();
    private final List<Packet<?>> serverPackets = new ArrayList<>();
    private boolean enabled;

    private FakeLag() {
        // we want to capture packets before any other handler processes them
        // these handlers will have a chance to process these packets later
        // when we re-send/re-receive them
        Events.BeforeSendPacket.add(this::onClientPacket, -1000);
        Events.BeforeHandlePacket.add(this::onServerPacket, -1000);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        if (enabled) {
            enabled = false;

            LocalPlayer player = mc.player;
            if (player != null) {
                PacketUtils.send(player, clientPackets);
                PacketUtils.receive(player, serverPackets);
            }
        } else {
            enabled = true;
        }
        clientPackets.clear();
        serverPackets.clear();
    }

    private boolean onClientPacket(Packet<?> packet) {
        if (enabled) {
            clientPackets.add(packet);
            return false;
        } else {
            return true;
        }
    }

    private boolean onServerPacket(Packet<?> packet) {
        if (enabled) {
            serverPackets.add(packet);
            return false;
        } else {
            return true;
        }
    }
}