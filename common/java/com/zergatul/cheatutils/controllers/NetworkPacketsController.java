package com.zergatul.cheatutils.controllers;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NetworkPacketsController {

    public static final NetworkPacketsController instance = new NetworkPacketsController();

    private final List<Consumer<ServerPacketArgs>> serverPacketHandlers = new ArrayList<>();
    private final List<Consumer<ClientPacketArgs>> clientPacketHandlers = new ArrayList<>();
    private Connection connection;
    private boolean handlersStopped;

    private NetworkPacketsController() {}

    public void addServerPacketHandler(Consumer<ServerPacketArgs> handler) {
        synchronized (serverPacketHandlers) {
            serverPacketHandlers.add(handler);
        }
    }

    public void addClientPacketHandler(Consumer<ClientPacketArgs> handler) {
        synchronized (clientPacketHandlers) {
            clientPacketHandlers.add(handler);
        }
    }

    public void sendPacket(Packet<?> packet) {
        if (connection != null) {
            connection.send(packet);
        }
    }

    public void receivePacket(Packet<?> packet) {
        if (connection != null) {
            connection.send(packet);
        }
    }

    public void stopHandlers() {
        handlersStopped = true;
    }

    public void resumeHandlers() {
        handlersStopped = false;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public boolean triggerReceive(Packet<?> packet) {
        if (!handlersStopped) {
            ServerPacketArgs args = new ServerPacketArgs();
            args.packet = packet;

            // do we need synchronized here???
            for (Consumer<ServerPacketArgs> handler : serverPacketHandlers) {
                handler.accept(args);
                if (args.skip) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean triggerSend(Packet<?> packet) {
        if (!handlersStopped) {
            ClientPacketArgs args = new ClientPacketArgs();
            args.packet = packet;

            // do we need synchronized here???
            for (Consumer<ClientPacketArgs> handler : clientPacketHandlers) {
                handler.accept(args);
                if (args.skip) {
                    return true;
                }
            }
        }

        return false;
    }

    public static class ServerPacketArgs {
        public Packet<?> packet;
        public boolean skip;
    }

    public static class ClientPacketArgs {
        public Packet<?> packet;
        public boolean skip;
    }
}
