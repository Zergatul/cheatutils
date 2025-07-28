package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.network.protocol.Packet;

@CustomType(name = "PacketEvent")
public class PacketEvent {

    private final Packet<?> packet;

    public PacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    @Getter(name = "packet")
    public Packet<?> getPacket() {
        return packet;
    }
}