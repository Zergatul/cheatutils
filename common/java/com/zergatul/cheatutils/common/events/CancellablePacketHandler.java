package com.zergatul.cheatutils.common.events;

import net.minecraft.network.protocol.Packet;

@FunctionalInterface
public interface CancellablePacketHandler {
    boolean handle(Packet<?> packet);
}