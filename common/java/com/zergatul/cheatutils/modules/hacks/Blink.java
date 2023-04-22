package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.entities.FakePlayer;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class Blink implements Module {

    public static final Blink instance = new Blink();

    private final Minecraft mc = Minecraft.getInstance();
    private final List<ServerboundMovePlayerPacket> packets = new ArrayList<>();
    private double startX, startY, startZ;
    private float startXRot, startYRot;
    private boolean isEnabled;
    private FakePlayer fake;

    private Blink() {
        NetworkPacketsController.instance.addClientPacketHandler(this::onClientPacket);
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

        NetworkPacketsController.instance.stopHandlers();
        packets.forEach(NetworkPacketsController.instance::sendPacket);
        packets.clear();
        NetworkPacketsController.instance.resumeHandlers();

        isEnabled = false;
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
            mc.player.moveTo(startX, startY, startZ, startYRot, startXRot);
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

    private void onClientPacket(NetworkPacketsController.ClientPacketArgs args) {
        if (!isEnabled) {
            return;
        }

        if (args.packet instanceof ServerboundMovePlayerPacket packet) {
            args.skip = true;

            if (packets.size() > 0) {
                ServerboundMovePlayerPacket prev = packets.get(packets.size() - 1);
                boolean same =
                        prev.getX(0) == packet.getX(0) &&
                        prev.getY(0) == packet.getY(0) &&
                        prev.getZ(0) == packet.getZ(0) &&
                        prev.getXRot(0) == packet.getXRot(0) &&
                        prev.getYRot(0) == packet.getYRot(0) &&
                        prev.isOnGround() == packet.isOnGround();
                if (same) {
                    return;
                }
            }

            packets.add(packet);
        }
    }
}