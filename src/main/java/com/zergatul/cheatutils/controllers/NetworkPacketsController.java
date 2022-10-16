package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import io.netty.channel.*;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NetworkPacketsController {

    public static final NetworkPacketsController instance = new NetworkPacketsController();

    private List<Consumer<ServerPacketArgs>> serverPacketHandlers = new ArrayList<>();
    private List<Consumer<ClientPacketArgs>> clientPacketHandlers = new ArrayList<>();
    private NetworkManager connection;

    private NetworkPacketsController() {
        ModApiWrapper.addOnClientPlayerLoggingIn(this::onClientPlayerLoggingIn);
    }

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

    public void sendPacket(IPacket<?> packet) {
        connection.send(packet);
    }

    public void receivePacket(IPacket<?> packet) {
        connection.channel().pipeline().fireChannelRead(packet);
    }

    private void onClientPlayerLoggingIn(NetworkManager connection) {
        this.connection = connection;
        ChannelPipeline pipeline = connection.channel().pipeline();

        synchronized (pipeline) {
            if (pipeline.get("PacketReader") == null) {
                pipeline.addBefore("packet_handler", "PacketReader", new SimpleChannelInboundHandler<IPacket<?>>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, IPacket<?> msg) throws Exception {

                        ServerPacketArgs args = new ServerPacketArgs();
                        args.packet = msg;

                        // do we need synchronized here???
                        for (Consumer<ServerPacketArgs> handler : serverPacketHandlers) {
                            handler.accept(args);
                            if (args.skip) {
                                return;
                            }
                        }

                        ctx.fireChannelRead(msg);
                    }
                });
            }

            if (pipeline.get("PacketWriter") == null) {
                pipeline.addBefore("packet_handler", "PacketWriter", new ChannelOutboundHandlerAdapter() {
                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {

                        ClientPacketArgs args = new ClientPacketArgs();
                        args.packet = (IPacket<?>) msg;

                        // do we need synchronized here???
                        for (Consumer<ClientPacketArgs> handler : clientPacketHandlers) {
                            handler.accept(args);
                            if (args.skip) {
                                promise.setSuccess();
                                return;
                            }
                        }

                        ctx.write(args.packet, promise);
                    }
                });
            }
        }
    }

    public static class ServerPacketArgs {
        public IPacket<?> packet;
        public boolean skip;
    }

    public static class ClientPacketArgs {
        public IPacket<?> packet;
        public boolean skip;
    }
}
