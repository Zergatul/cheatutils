package com.zergatul.cheatutils.controllers;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TeleportHackController {

    public static final TeleportHackController instance = new TeleportHackController();

    private final Minecraft mc = Minecraft.getInstance();

    private TeleportHackController() {

    }

    public boolean teleportToCrosshair(double distance, int repeats) {
        if (mc.player == null || mc.level == null) {
            return false;
        }

        float partialTicks = mc.getPartialTick();
        HitResult result = mc.player.pick(distance, partialTicks, false);
        if (result instanceof BlockHitResult blockHitResult) {
            Vec3 target = blockHitResult.getLocation();
            return teleport(target, repeats);
        } else {
            return false;
        }
    }

    public boolean verticalTeleport(double distance, int repeats) {
        if (mc.player == null || mc.level == null) {
            return false;
        }

        float partialTicks = mc.getPartialTick();
        Vec3 pos = mc.player.getPosition(partialTicks);
        return teleport(pos.add(0, distance, 0), repeats);
    }

    private boolean teleport(Vec3 target, int repeats) {
        if (mc.player == null || mc.level == null) {
            return false;
        }

        // skip teleports for such distances
        final double minDistanceSqr = 1;

        float partialTicks = mc.getPartialTick();
        Vec3 pos = mc.player.getPosition(partialTicks);
        if (pos.distanceToSqr(target) < minDistanceSqr) {
            return false;
        }

        EntityDimensions dimensions = mc.player.getDimensions(mc.player.getPose());
        Vec3 direction = target.subtract(pos).normalize();

        while (true) {
            if (pos.distanceToSqr(target) < minDistanceSqr) {
                return false;
            }

            Iterable<VoxelShape> collisions = mc.level.getBlockCollisions(mc.player, dimensions.makeBoundingBox(target));
            if (collisions.iterator().hasNext()) {
                // move backwards
                double mult = dimensions.width * 0.1;
                target = target.subtract(direction.x * mult, direction.y * mult, direction.z * mult);
            } else {
                // no collision
                break;
            }
        }

        for (int i = 0 ; i < repeats; i++) {
            NetworkPacketsController.instance.sendPacket(
                    new ServerboundMovePlayerPacket.Pos(pos.x, pos.y, pos.z, mc.player.isOnGround()));
        }

        NetworkPacketsController.instance.sendPacket(
                new ServerboundMovePlayerPacket.Pos(target.x, target.y, target.z, false));

        mc.player.moveTo(target);

        return true;
    }
}