package com.zergatul.cheatutils.common.events;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;

@FunctionalInterface
public interface S2CPacketHandler {
    boolean handle(PacketListener listener, Packet<?> packet);
}