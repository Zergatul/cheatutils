package com.zergatul.cheatutils.controllers;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class NetworkPacketsController {

    public static final NetworkPacketsController instance = new NetworkPacketsController();

    // Script crash cleanup can detach handlers while the network thread is dispatching a packet.
    private final List<Consumer<ServerPacketArgs>> serverPacketHandlers = new CopyOnWriteArrayList<>();
    private final List<Consumer<ClientPacketArgs>> clientPacketHandlers = new CopyOnWriteArrayList<>();
    private volatile Connection connection;
    private volatile boolean handlersStopped;

    private NetworkPacketsController() {}

    public void addServerPacketHandler(Consumer<ServerPacketArgs> handler) {
        synchronized (serverPacketHandlers) {
            serverPacketHandlers.add(handler);
        }
    }

    public void addServerPacketHandlerIfAbsent(Consumer<ServerPacketArgs> handler) {
        synchronized (serverPacketHandlers) {
            if (!serverPacketHandlers.contains(handler)) {
                serverPacketHandlers.add(handler);
            }
        }
    }

    public void removeServerPacketHandler(Consumer<ServerPacketArgs> handler) {
        synchronized (serverPacketHandlers) {
            serverPacketHandlers.remove(handler);
        }
    }

    public void addClientPacketHandler(Consumer<ClientPacketArgs> handler) {
        synchronized (clientPacketHandlers) {
            clientPacketHandlers.add(handler);
        }
    }

    public void addClientPacketHandlerIfAbsent(Consumer<ClientPacketArgs> handler) {
        synchronized (clientPacketHandlers) {
            if (!clientPacketHandlers.contains(handler)) {
                clientPacketHandlers.add(handler);
            }
        }
    }

    public void removeClientPacketHandler(Consumer<ClientPacketArgs> handler) {
        synchronized (clientPacketHandlers) {
            clientPacketHandlers.remove(handler);
        }
    }

    public void sendPacket(Packet<?> packet) {
        if (connection != null) {
            connection.send(packet);
        }
    }

    public void receivePacket(Packet<?> packet) {
        if (connection != null) {
            try {
                connection.channelRead(null, packet);
            } catch (Exception e) {
                LogManager.getLogger(NetworkPacketsController.class).error("Cannot receive packet", e);
            }
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