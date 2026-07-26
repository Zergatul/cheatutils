package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.PlayerReleaseUsingItemEvent;
import com.zergatul.cheatutils.common.events.PlayerTurnByMouseEvent;
import com.zergatul.cheatutils.configs.AimAssistConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.utils.Rotation;
import com.zergatul.cheatutils.utils.RotationUtils;
import com.zergatul.cheatutils.utils.ServerBehavior;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class AimAssist implements Module {

    public static final AimAssist instance = new AimAssist();

    private final Minecraft mc = Minecraft.getInstance();

    private boolean isTargetLockEnabled;
    private Entity bowAssistTarget;
    private Entity targetLockEntity;

    private AimAssist() {
        Events.InGameTickEnd.add(this::onTickEnd);
        Events.PlayerReleaseUsingItem.add(this::onPlayerReleaseUsingItem);
        Events.RenderTickStart.add(this::onRenderTickStart);
        Events.PlayerTurnByMouse.add(this::onPlayerTurnByMouse);
    }

    public boolean isTargetLockEnabled() {
        return isTargetLockEnabled;
    }

    public void enableTargetLock() {
        isTargetLockEnabled = true;
    }

    public void disableTargetLock() {
        isTargetLockEnabled = false;
    }

    public Entity getBowAssistTarget() {
        return bowAssistTarget;
    }

    private void onTickEnd() {
        bowAssistTarget = null;

        if (!ConfigStore.instance.getConfig().aimAssist.bowAssist) {
            return;
        }
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (!mc.player.getItemInHand(InteractionHand.MAIN_HAND).is(Items.BOW)) {
            return;
        }
        if (!mc.player.isUsingItem()) {
            return;
        }

        Rotation playerRot = new Rotation(mc.player.getXRot(), mc.player.getYRot());
        if (bowAssistTarget == null) {
            bowAssistTarget = findTarget(playerRot);
            if (bowAssistTarget == null) {
                return;
            }
        }

        int ticks = mc.player.getTicksUsingItem();
        float power = BowItem.getPowerForTime(ticks);
        float speed = power * 3;

        List<Rotation> rotations = findRotations(mc.player, speed);
        if (rotations.isEmpty()) {
            bowAssistTarget = null;
        }
    }

    private void onPlayerReleaseUsingItem(PlayerReleaseUsingItemEvent event) {
        if (!ConfigStore.instance.getConfig().aimAssist.bowAssist) {
            return;
        }
        if (mc.player == null || mc.level == null || bowAssistTarget == null) {
            return;
        }

        Rotation playerRot = new Rotation(mc.player.getXRot(), mc.player.getYRot());

        int ticks = mc.player.getTicksUsingItem();
        float power = BowItem.getPowerForTime(ticks);
        float speed = power * 3;

        List<Rotation> rotations = findRotations(mc.player, speed);
        Optional<Rotation> closest = rotations.stream().min(Comparator.comparingDouble(playerRot::distanceSqrTo));
        if (closest.isPresent()) {
            Rotation rotation = closest.get();
            NetworkPacketsController.instance.sendPacket(new ServerboundMovePlayerPacket.Rot(
                    rotation.yRot(), rotation.xRot(),
                    mc.player.onGround(), mc.player.horizontalCollision));
        }
    }

    private void onRenderTickStart(DeltaTracker delta) {
        if (mc.player == null || !isTargetLockEnabled) {
            targetLockEntity = null;
            return;
        }

        float partialTicks = delta.getGameTimeDeltaPartialTick(true);
        if (targetLockEntity == null) {
            Rotation rotation = new Rotation(mc.player.getXRot(partialTicks), mc.player.getYRot(partialTicks));
            targetLockEntity = findTarget(rotation);
        }

        if (targetLockEntity != null) {
            AABB box = targetLockEntity.getDimensions(targetLockEntity.getPose()).makeBoundingBox(targetLockEntity.getPosition(partialTicks));

            AimAssistConfig config = ConfigStore.instance.getConfig().aimAssist;
            Vec3 target;
            if (AimAssistConfig.AIM_ASSIST_HEAD.equals(config.aimAssistMode)) {
                target = new Vec3(Mth.lerp(0.5, box.minX, box.maxX), box.maxY, Mth.lerp(0.5, box.minZ, box.maxZ));
            } else {
                target = box.getCenter();
            }

            Rotation rotation = RotationUtils.getRotation(mc.player.getEyePosition(partialTicks), target);
            mc.player.setXRot(rotation.xRot());
            mc.player.setYRot(rotation.yRot());
        }
    }

    private void onPlayerTurnByMouse(PlayerTurnByMouseEvent event) {
        if (targetLockEntity != null) {
            event.cancel();
        }
    }

    private Entity findTarget(Rotation playerRot) {
        assert mc.level != null && mc.player != null;

        Entity target = null;
        double bestDeltaAngleSqr = Double.MAX_VALUE;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player) {
                continue;
            }
            if (entity instanceof LivingEntity) {
                Vec3 center = getEntityCenter(entity);
                Rotation rotation = RotationUtils.getRotation(mc.player.getEyePosition(), center);
                double deltaAngleSqr = playerRot.distanceSqrTo(rotation);
                if (deltaAngleSqr < bestDeltaAngleSqr) {
                    bestDeltaAngleSqr = deltaAngleSqr;
                    target = entity;
                }
            }
        }

        return target;
    }

    private List<Rotation> findRotations(LocalPlayer player, float speed) {
        assert bowAssistTarget != null;

        Rotation straight = RotationUtils.getRotation(player.getEyePosition(), bowAssistTarget.position());
        Rotation rot1 = findRotation(player, speed, straight.withXRot(-90));
        Rotation rot2 = findRotation(player, speed, straight.withXRot(90));
        if (rot1 == null && rot2 == null) {
            return List.of();
        }
        if (rot1 == null) {
            return List.of(rot2);
        }
        if (rot2 == null) {
            return List.of(rot1);
        }
        if (rot1.approximateEquals(rot2, 0.05F)) {
            return List.of(rot1);
        }
        return List.of(rot1, rot2);
    }

    private Rotation findRotation(LocalPlayer player, float speed, Rotation initial) {
        Rotation rotation = initial;
        double bestDistance = calculatePath(player, speed, rotation.yRot(), rotation.xRot()).getClosestDistance();

        float delta = 5;
        while (delta > 0.02F) {
            Path path;
            boolean changed = false;

            path = calculatePath(player, speed, rotation.yRot() + delta, rotation.xRot());
            if (path.getClosestDistance() < bestDistance) {
                rotation = rotation.addYRot(delta);
                bestDistance = path.getClosestDistance();
                changed = true;
            }

            path = calculatePath(player, speed, rotation.yRot() - delta, rotation.xRot());
            if (path.getClosestDistance() < bestDistance) {
                rotation = rotation.addYRot(-delta);
                bestDistance = path.getClosestDistance();
                changed = true;
            }

            if (rotation.xRot() + delta <= 90) {
                path = calculatePath(player, speed, rotation.yRot(), rotation.xRot() + delta);
                if (path.getClosestDistance() < bestDistance) {
                    rotation = rotation.addXRot(delta);
                    bestDistance = path.getClosestDistance();
                    changed = true;
                }
            }

            if (rotation.xRot() - delta >= -90) {
                path = calculatePath(player, speed, rotation.yRot(), rotation.xRot() - delta);
                if (path.getClosestDistance() < bestDistance) {
                    rotation = rotation.addXRot(-delta);
                    bestDistance = path.getClosestDistance();
                    changed = true;
                }
            }

            if (!changed) {
                delta /= 5;
            }
        }

        if (bestDistance < bowAssistTarget.getBbWidth() / 2) {
            return rotation;
        } else {
            return null;
        }
    }

    private Path calculatePath(LocalPlayer player, float speed, float yRot, float xRot) {
        float speedX = -Mth.sin(yRot * ((float)Math.PI / 180F)) * Mth.cos(xRot * ((float)Math.PI / 180F));
        float speedY = -Mth.sin(xRot * ((float)Math.PI / 180F));
        float speedZ = Mth.cos(yRot * ((float)Math.PI / 180F)) * Mth.cos(xRot * ((float)Math.PI / 180F));
        Vec3 deltaMovement = new Vec3(speedX, speedY, speedZ)
                .normalize()
                .scale(speed)
                .add(ServerBehavior.predictPlayerKnownMovement());

        Path path = new Path();
        path.entityPosition[0] = getEntityCenter(bowAssistTarget);
        path.arrowPosition[0] = player.getEyePosition();

        Vec3 entityDeltaMovement = getEntitySpeed(bowAssistTarget);
        for (int i = 1; i < 200; i++) {
            path.entityPosition[i] = path.entityPosition[i - 1].add(entityDeltaMovement);
            path.arrowPosition[i] = path.arrowPosition[i - 1].add(deltaMovement);
            deltaMovement = deltaMovement.scale(0.99F).add(0, -0.05, 0);
        }

        return path;
    }

    private Vec3 getEntityCenter(Entity entity) {
        return entity.position().add(0, entity.getBbHeight() / 2, 0);
    }

    private Vec3 getEntitySpeed(Entity entity) {
        Entity vehicle = entity.getVehicle();
        if (vehicle != null) {
            return getEntitySpeed(vehicle);
        }

        return new Vec3(entity.getX() - entity.xo, entity.getY() - entity.yo, entity.getZ() - entity.zo);
    }

    private static class Path {

        public final Vec3[] entityPosition;
        public final Vec3[] arrowPosition;

        public Path() {
            entityPosition = new Vec3[200];
            arrowPosition = new Vec3[200];
        }

        public double getClosestDistance() {
            double closest = Double.MAX_VALUE;
            for (int i = 1; i < 200; i++) {
                double distanceSqr = getPointToLineSegmentDistanceSqr(entityPosition[i], arrowPosition[i - 1], arrowPosition[i]);
                if (distanceSqr < closest) {
                    closest = distanceSqr;
                }
            }
            return Math.sqrt(closest);
        }

        private double getPointToLineSegmentDistanceSqr(Vec3 point, Vec3 line1, Vec3 line2) {
            Vec3 lineVector = line2.subtract(line1);
            Vec3 pointVec1 = point.subtract(line1);

            // Point is lagging behind start of the segment, so perpendicular distance is not viable.
            // Use distance to start of segment instead.
            if (pointVec1.dot(lineVector) <= 0) {
                return pointVec1.lengthSqr();
            }

            Vec3 pointVec2 = point.subtract(line2);

            // Point is advanced past the end of the segment, so perpendicular distance is not viable.
            // Use distance to end of the segment instead.
            if (pointVec2.dot(lineVector) >= 0) {
                return pointVec2.lengthSqr();
            }

            return lineVector.cross(pointVec1).lengthSqr() / lineVector.lengthSqr();
        }
    }
}