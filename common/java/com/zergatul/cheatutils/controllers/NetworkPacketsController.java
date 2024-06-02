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
    private volatile Connection connection;
    private volatile boolean handlersStopped;

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

    public void onConnect(Connection connection) {
        this.connection = connection;
    }

    public void onDisconnect(Connection connection) {
        if (this.connection == connection) {
            this.connection = null;
        }
    }

    public boolean triggerReceive(Connection connection, Packet<?> packet) {
        if (!handlersStopped && connection == this.connection) {
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

    public boolean triggerSend(Connection connection, Packet<?> packet) {
        if (!handlersStopped && connection == this.connection) {
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