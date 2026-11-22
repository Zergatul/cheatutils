package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.extensions.ConnectionExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketProcessor;
import net.minecraft.network.protocol.Packet;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public class PacketUtils {

    private final static PacketProcessor packetProcessor = Minecraft.getInstance().packetProcessor();

    public static void send(LocalPlayer player, Packet<?> packet) {
        player.connection.getConnection().send(packet, null, true);
    }

    public static void send(LocalPlayer player, Packet<?>... packets) {
        Connection connection = player.connection.getConnection();
        for (int i = 0; i < packets.length; i++) {
            Packet<?> packet = packets[i];
            boolean flush = i == packets.length - 1;
            connection.send(packet, null, flush);
        }
    }

    public static void send(LocalPlayer player, List<Packet<?>> packets) {
        Connection connection = player.connection.getConnection();
        for (int i = 0; i < packets.size(); i++) {
            Packet<?> packet = packets.get(i);
            boolean flush = i == packets.size() - 1;
            connection.send(packet, null, flush);
        }
    }

    public static void sendWithBypass(LocalPlayer player, List<Packet<?>> packets) {
        Connection connection = player.connection.getConnection();
        ((ConnectionExtension) connection).setBypassSendPacketEvent_CU(true);
        try {
            for (int i = 0; i < packets.size(); i++) {
                Packet<?> packet = packets.get(i);
                boolean flush = i == packets.size() - 1;
                connection.send(packet, null, flush);
            }
        } finally {
            ((ConnectionExtension) connection).setBypassSendPacketEvent_CU(false);
        }
    }

    @SuppressWarnings("unchecked")
    public static void receive(LocalPlayer player, Packet<?> packet) {
        packetProcessor.scheduleIfPossible(player.connection, (Packet<? super ClientPacketListener>) packet);
    }

    @SuppressWarnings("unchecked")
    public static void receive(LocalPlayer player, List<Packet<?>> packets) {
        for (Packet<?> packet : packets) {
            packetProcessor.scheduleIfPossible(player.connection, (Packet<? super ClientPacketListener>) packet);
        }
    }
}