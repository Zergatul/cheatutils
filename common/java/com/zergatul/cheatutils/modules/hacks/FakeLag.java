package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.utils.PacketUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;

import java.util.ArrayList;
import java.util.List;

public class FakeLag implements Module {

    public static final FakeLag INSTANCE = new FakeLag();

    private final List<PacketUtils.CapturedClientPacket> clientPackets = new ArrayList<>();
    private final List<PacketUtils.CapturedServerPacket> serverPackets = new ArrayList<>();
    private boolean enabled;

    private FakeLag() {
        // we want to capture packets before any other handler processes them
        // these handlers will have a chance to process these packets later
        // when we re-send/re-receive them
        Events.BeforeSendPacketClientThread.add(this::onClientPacket, -1000);
        Events.BeforeHandlePacketClientThread.add(this::onServerPacket, -1000);
        Events.ClientPlayerLoggingOut.add(this::onLoggingOut);

        // TODO: discover idea to have config switch:
        // - skip all packets by using BeforeSendPacketNettyThread
        // - skip only essential packets that goes through BeforeSendPacketClientThread (current)
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        if (enabled) {
            enabled = false;
            List<PacketUtils.CapturedClientPacket> clientPackets = List.copyOf(this.clientPackets);
            List<PacketUtils.CapturedServerPacket> serverPackets = List.copyOf(this.serverPackets);
            this.clientPackets.clear();
            this.serverPackets.clear();

            PacketUtils.send(clientPackets);
            PacketUtils.receive(serverPackets);
        } else {
            enabled = true;
            clientPackets.clear();
            serverPackets.clear();
        }
    }

    private boolean onClientPacket(Connection connection, Packet<?> packet) {
        if (enabled) {
            clientPackets.add(new PacketUtils.CapturedClientPacket(connection, packet));
            return false;
        } else {
            return true;
        }
    }

    private boolean onServerPacket(PacketListener listener, Packet<?> packet) {
        if (enabled) {
            serverPackets.add(new PacketUtils.CapturedServerPacket(listener, packet));
            return false;
        } else {
            return true;
        }
    }

    private void onLoggingOut() {
        if (enabled) {
            enabled = false;
            // connection is already closed, no point to replay packets
            clientPackets.clear();
            serverPackets.clear();
        }
    }
}