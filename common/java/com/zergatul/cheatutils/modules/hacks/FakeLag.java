package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.network.protocol.Packet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FakeLag implements Module {

    public static final FakeLag instance = new FakeLag();

    private volatile boolean enabled;
    private final List<Packet<?>> clientPackets = Collections.synchronizedList(new ArrayList<>());
    private final List<Packet<?>> serverPackets = Collections.synchronizedList(new ArrayList<>());

    private FakeLag() {
        NetworkPacketsController.instance.addClientPacketHandler(this::onClientPacket);
        NetworkPacketsController.instance.addServerPacketHandler(this::onServerPacket);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        if (enabled) {
            enabled = false;
            clientPackets.forEach(NetworkPacketsController.instance::sendPacket);
            serverPackets.forEach(NetworkPacketsController.instance::receivePacket);
        } else {
            enabled = true;
        }
        clientPackets.clear();
        serverPackets.clear();
    }

    private void onClientPacket(NetworkPacketsController.ClientPacketArgs args) {
        if (enabled) {
            clientPackets.add(args.packet);
            args.skip = true;
        }
    }

    private void onServerPacket(NetworkPacketsController.ServerPacketArgs args) {
        if (enabled) {
            serverPackets.add(args.packet);
            args.skip = true;
        }
    }
}