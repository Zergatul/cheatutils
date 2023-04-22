package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.mixins.common.accessors.ConnectionAccessor;
import io.netty.channel.*;
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

    private NetworkPacketsController() {
        Events.ClientPlayerLoggingIn.add(this::onClientPlayerLoggingIn);
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

    public void sendPacket(Packet<?> packet) {
        connection.send(packet);
    }

    public void receivePacket(Packet<?> packet) {
        ((ConnectionAccessor) connection).getChannel_CU().pipeline().fireChannelRead(packet);
    }

    public void stopHandlers() {
        handlersStopped = true;
    }

    public void resumeHandlers() {
        handlersStopped = false;
    }

    private void onClientPlayerLoggingIn(Connection connection) {
        this.connection = connection;
        ChannelPipeline pipeline = ((ConnectionAccessor) connection).getChannel_CU().pipeline();

        synchronized (pipeline) {
            if (pipeline.get("PacketReader") == null) {
                pipeline.addBefore("packet_handler", "PacketReader", new SimpleChannelInboundHandler<Packet<?>>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Packet<?> msg) throws Exception {
                        if (!handlersStopped) {
                            ServerPacketArgs args = new ServerPacketArgs();
                            args.packet = msg;

                            // do we need synchronized here???
                            for (Consumer<ServerPacketArgs> handler : serverPacketHandlers) {
                                handler.accept(args);
                                if (args.skip) {
                                    return;
                                }
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
                        if (!handlersStopped) {
                            ClientPacketArgs args = new ClientPacketArgs();
                            args.packet = (Packet<?>) msg;

                            // do we need synchronized here???
                            for (Consumer<ClientPacketArgs> handler : clientPacketHandlers) {
                                handler.accept(args);
                                if (args.skip) {
                                    promise.setSuccess();
                                    return;
                                }
                            }
                            ctx.write(args.packet, promise);
                        } else {
                            ctx.write(msg, promise);
                        }
                    }
                });
            }
        }
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
