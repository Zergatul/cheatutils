package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.Setter;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.network.protocol.Packet;

@CustomType(name = "PacketEvent")
public class PacketEvent {

    private final Packet<?> packet;
    private boolean cancelled;

    public PacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    @Getter(name = "packet")
    public Packet<?> getPacket() {
        return packet;
    }

    @Getter(name = "cancel")
    public boolean getCancel() {
        return cancelled;
    }

    @Setter(name = "cancel")
    public void setCancel(boolean value) {
        cancelled = value;
    }
}