package com.zergatul.cheatutils.controllers;

import io.netty.channel.*;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NetworkPacketsController {

    public static final NetworkPacketsController instance = new NetworkPacketsController();

    private final Logger logger = LogManager.getLogger(NetworkPacketsController.class);
    private List<Consumer<ServerPacketArgs>> serverPacketHandlers = new ArrayList<>();
    private List<Consumer<ClientPacketArgs>> clientPacketHandlers = new ArrayList<>();
    private Connection connection;

    private NetworkPacketsController() {

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

    /*public void sendClientPacket(IPacket<?> packet) {
        manager.channel().pipeline().fireChannelRead(packet);
    }*/

    @SubscribeEvent
    public void onClientPlayerLoggedIn(ClientPlayerNetworkEvent.LoggedInEvent event) {

        connection = event.getConnection();
        ChannelPipeline pipeline = connection.channel().pipeline();

        synchronized (pipeline) {

            if (pipeline.get("PacketReader") == null) {
                pipeline.addBefore("packet_handler", "PacketReader", new SimpleChannelInboundHandler<Packet<?>>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Packet<?> msg) throws Exception {

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

                logger.debug("Packet reader pipeline attached");
            } else {
                logger.debug("Packet reader pipeline already exists");
            }

            if (pipeline.get("PacketWriter") == null) {
                pipeline.addBefore("packet_handler", "PacketWriter", new ChannelOutboundHandlerAdapter() {
                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {

                        ClientPacketArgs args = new ClientPacketArgs();
                        args.packet = (Packet<?>)msg;

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
        public Packet<?> packet;
        public boolean skip;
    }

    public static class ClientPacketArgs {
        public Packet<?> packet;
        public boolean skip;
    }

}
