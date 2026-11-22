package com.zergatul.cheatutils.common.events;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;

@FunctionalInterface
public interface C2SPacketHandler {
    boolean handle(Connection connection, Packet<?> packet);
}