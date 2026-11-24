package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.entities.FakePlayer;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.utils.PacketUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Blink implements Module {

    public static final Blink INSTANCE = new Blink();

    private final Minecraft mc = Minecraft.getInstance();
    private final List<Packet<?>> packets = new ArrayList<>();
    private double startX, startY, startZ;
    private float startXRot, startYRot;
    private boolean isEnabled;
    private FakePlayer fake;

    private Blink() {
        // we want to capture movement/tick packets after every other handler, including user handlers
        Events.BeforeSendPacketClientThread.add(this::onClientPacket, 3000);
        Events.ClientPlayerLoggingOut.add(this::disable);
        Events.DimensionChange.add(this::disable);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void apply() {
        if (!isEnabled) {
            return;
        }

        isEnabled = false;

        // packets were already processed by all handlers
        // no need to process them again
        PacketUtils.sendWithBypass(Objects.requireNonNull(mc.player), packets);
        packets.clear();

        fake.remove(Entity.RemovalReason.DISCARDED);
    }

    public void enable() {
        if (isEnabled) {
            return;
        }

        if (mc.player == null) {
            return;
        }

        isEnabled = true;
        packets.clear();

        startX = mc.player.getX();
        startY = mc.player.getY();
        startZ = mc.player.getZ();
        startXRot = mc.player.getXRot();
        startYRot = mc.player.getYRot();

        fake = new FakePlayer(mc.player);
    }

    public void disable() {
        if (!isEnabled) {
            return;
        }

        isEnabled = false;
        packets.clear();

        if (mc.player != null) {
            mc.player.snapTo(startX, startY, startZ, startYRot, startXRot);
        }

        fake.remove(Entity.RemovalReason.DISCARDED);
    }

    public double getDistance() {
        if (!isEnabled) {
            return 0;
        }

        if (mc.player == null) {
            return 0;
        }

        double dx = startX - mc.player.getX();
        double dy = startY - mc.player.getY();
        double dz = startZ - mc.player.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public int getPackets() {
        if (!isEnabled) {
            return 0;
        } else {
            return packets.size();
        }
    }

    private boolean onClientPacket(Connection connection, Packet<?> packet) {
        if (!isEnabled) {
            return true;
        }

        if (packet instanceof ServerboundMovePlayerPacket || packet instanceof ServerboundClientTickEndPacket) {
            packets.add(packet);
            return false;
        }

        return true;
    }
}