package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.modules.esp.FreeCam;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TeleportHack {

    public static final TeleportHack instance = new TeleportHack();

    private final Minecraft mc = Minecraft.getInstance();

    private TeleportHack() {}

    public boolean teleportToCrosshair(double distance) {
        return teleportToCrosshair(distance, this::calculatePackets, true);
    }

    public boolean teleportToCrosshair(double distance, int packets) {
        return teleportToCrosshair(distance, actualDistance -> packets, false);
    }

    public boolean verticalTeleport(double distance) {
        return verticalTeleport(distance, Math.signum(distance), false, this::calculatePackets, true);
    }

    public boolean verticalTeleport(double distance, int packets) {
        return verticalTeleport(distance, Math.signum(distance), false, actualDistance -> packets, false);
    }

    public boolean verticalTeleport(double from, double to, boolean findSurface) {
        return verticalTeleport(from, to, findSurface, this::calculatePackets, true);
    }

    public boolean verticalTeleport(double from, double to, boolean findSurface, int packets) {
        return verticalTeleport(from, to, findSurface, actualDistance -> packets, false);
    }

    private boolean verticalTeleport(double from, double to, boolean findSurface, PacketsCountCalculator calculator, boolean enforceVanillaPacketBudget) {
        if (Math.signum(from) != Math.signum(to)) {
            return false;
        }

        if (Math.abs(from) < 1 || Math.abs(to) < 1) {
            return false;
        }

        if (mc.player == null || mc.level == null) {
            return false;
        }

        Vec3 pos = mc.player.position();
        return teleport(pos.add(0, from, 0), pos.add(0, to, 0), findSurface, calculator, enforceVanillaPacketBudget);
    }

    private boolean teleportToCrosshair(double distance, PacketsCountCalculator calculator, boolean enforceVanillaPacketBudget) {
        if (distance <= 0) {
            return false;
        }

        if (mc.player == null || mc.level == null) {
            return false;
        }

        BlockHitResult hit = rayCast(distance);
        Vec3 origin = mc.player.position();
        Vec3 destination = hit.getLocation();
        double lengthSqr = destination.distanceToSqr(origin);
        Vec3 direction = destination.subtract(origin).normalize();

        Vec3 current = destination;
        while (current.distanceToSqr(destination) < lengthSqr) {
            if (wouldServerRejectMovement(current)) {
                // move position 0.1 blocks closer to origin
                current = current.subtract(direction.multiply(new Vec3(0.1, 0.1, 0.1)));
            } else {
                int packets = calculator.calculate(origin.distanceTo(current));
                if (exceedsVanillaPacketBudget(packets, enforceVanillaPacketBudget)) {
                    current = current.subtract(direction.multiply(new Vec3(0.1, 0.1, 0.1)));
                    continue;
                }

                sendTeleport(current, packets);
                return true;
            }
        }

        return false;
    }

    private int calculatePackets(double distance) {
        return Mth.ceil(distance * distance / 100);
    }

    private boolean teleport(Vec3 from, Vec3 to, boolean findSurface, PacketsCountCalculator calculator, boolean enforceVanillaPacketBudget) {
        if (mc.player == null || mc.level == null) {
            return false;
        }

        double maxDistanceSqr = from.distanceToSqr(to);
        if (maxDistanceSqr < 0.01f) {
            return false;
        }

        EntityDimensions dimensions = mc.player.getDimensions(mc.player.getPose());
        Vec3 direction = to.subtract(from).normalize();

        Vec3 target = from;
        while (true) {
            if (from.distanceToSqr(target) > maxDistanceSqr) {
                return false;
            }

            Iterable<VoxelShape> collisions = mc.level.getBlockCollisions(mc.player, dimensions.makeBoundingBox(target));
            if (!collisions.iterator().hasNext()) {
                // no collisions
                boolean found = false;
                if (findSurface) {
                    Iterable<VoxelShape> belowCollisions = mc.level.getBlockCollisions(
                            mc.player,
                            dimensions.makeBoundingBox(target.add(0, -0.1, 0)));
                    if (belowCollisions.iterator().hasNext()) {
                        found = true;
                    }
                } else {
                    found = true;
                }

                if (found) {
                    if (!wouldServerRejectMovement(target)) {
                        int packets = calculator.calculate(mc.player.position().distanceTo(target));
                        if (!exceedsVanillaPacketBudget(packets, enforceVanillaPacketBudget)) {
                            sendTeleport(target, packets);
                            return true;
                        }
                    }
                }
            }

            target = target.add(direction.multiply(0.1, 0.1, 0.1));
        }
    }

    private boolean exceedsVanillaPacketBudget(int packets, boolean enforceVanillaPacketBudget) {
        return enforceVanillaPacketBudget && packets > 5;
    }

    private void sendTeleport(Vec3 target, int packets) {
        assert mc.player != null;

        for (int i = 0; i < packets - 1; i++) {
            NetworkPacketsController.instance.sendPacket(
                    new ServerboundMovePlayerPacket.StatusOnly(mc.player.onGround(), mc.player.horizontalCollision));
        }

        NetworkPacketsController.instance.sendPacket(
                new ServerboundMovePlayerPacket.Pos(
                        target.x, target.y, target.z,
                        willBeOnGround(target),
                        true));

        mc.player.snapTo(target);
    }

    private BlockHitResult rayCast(double distance) {
        assert mc.level != null;
        assert mc.player != null;

        Vec3 origin = FreeCam.instance.isActive() ? FreeCam.instance.getPosition() : mc.player.getEyePosition();
        Vec3 direction = FreeCam.instance.isActive() ? FreeCam.instance.getViewVector() : mc.player.getViewVector(1f);
        Vec3 destination = origin.add(direction.x * distance, direction.y * distance, direction.z * distance);
        ClipContext context = new ClipContext(origin, destination, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player);
        return BlockGetter.traverseBlocks(origin, destination, context, (ctx, pos) -> {
            BlockState state = mc.level.getBlockState(pos);
            if (state.getCollisionShape(mc.level, pos).isEmpty()) {
                return null;
            }

            VoxelShape shape = ctx.getBlockShape(state, mc.level, pos);
            return mc.level.clipWithInteractionOverride(ctx.getFrom(), ctx.getTo(), pos, shape, state);
        }, ctx -> {
            Vec3 missDirection = ctx.getFrom().subtract(ctx.getTo());
            return BlockHitResult.miss(
                    ctx.getTo(),
                    Direction.getApproximateNearest(missDirection.x, missDirection.y, missDirection.z),
                    BlockPos.containing(ctx.getTo()));
        });
    }

    private boolean wouldServerRejectMovement(Vec3 target) {
        assert mc.level != null;
        assert mc.player != null;

        if (mc.player.noPhysics || mc.player.isSleeping()) {
            return false;
        }

        AABB oldBox = mc.player.getBoundingBox();
        Vec3 targetMovement = target.subtract(mc.player.position());
        Vec3 resolvedMovement = collideLikeServer(targetMovement, oldBox);
        Vec3 unresolvedMovement = targetMovement.subtract(resolvedMovement);
        if (unresolvedMovement.y > -0.5 || unresolvedMovement.y < 0.5) {
            unresolvedMovement = new Vec3(unresolvedMovement.x, 0, unresolvedMovement.z);
        }

        boolean movedWrongly =
                unresolvedMovement.lengthSqr() > 0.0625 &&
                !mc.player.isCreative() &&
                !mc.player.isSpectator() &&
                !mc.player.isInPostImpulseGraceTime();

        return movedWrongly && mc.level.noCollision(mc.player, oldBox) || isEntityCollidingWithAnythingNew(oldBox, target);
    }

    private Vec3 collideLikeServer(Vec3 movement, AABB box) {
        assert mc.level != null;
        assert mc.player != null;

        if (movement.lengthSqr() == 0) {
            return movement;
        }

        List<VoxelShape> entityCollisions = mc.level.getEntityCollisions(mc.player, box.expandTowards(movement));
        Vec3 resolved = Entity.collideBoundingBox(mc.player, movement, box, mc.level, entityCollisions);
        boolean xCollision = movement.x != resolved.x;
        boolean yCollision = movement.y != resolved.y;
        boolean zCollision = movement.z != resolved.z;
        boolean onGroundAfterCollision = yCollision && movement.y < 0;

        if (mc.player.maxUpStep() > 0 && (onGroundAfterCollision || mc.player.onGround()) && (xCollision || zCollision)) {
            AABB groundedBox = onGroundAfterCollision ? box.move(0, resolved.y, 0) : box;
            AABB stepUpBox = groundedBox.expandTowards(movement.x, mc.player.maxUpStep(), movement.z);
            if (!onGroundAfterCollision) {
                stepUpBox = stepUpBox.expandTowards(0, -1.0E-5F, 0);
            }

            List<VoxelShape> stepCollisions = Entity.collectAllColliders(mc.player, mc.level, stepUpBox);
            float skippedHeight = (float) resolved.y;
            for (float stepHeight : collectCandidateStepUpHeights(groundedBox, stepCollisions, mc.player.maxUpStep(), skippedHeight)) {
                Vec3 stepMovement = collideWithShapes(new Vec3(movement.x, stepHeight, movement.z), groundedBox, stepCollisions);
                if (stepMovement.horizontalDistanceSqr() > resolved.horizontalDistanceSqr()) {
                    double distanceToGround = box.minY - groundedBox.minY;
                    return stepMovement.subtract(0, distanceToGround, 0);
                }
            }
        }

        return resolved;
    }

    private List<Float> collectCandidateStepUpHeights(AABB box, List<VoxelShape> collisions, float maxStepHeight, float skippedHeight) {
        List<Float> heights = new ArrayList<>();
        for (VoxelShape collision : collisions) {
            for (double coord : collision.getCoords(Direction.Axis.Y)) {
                float height = (float) (coord - box.minY);
                if (height < 0 || height == skippedHeight) {
                    continue;
                }
                if (height > maxStepHeight) {
                    break;
                }
                if (!heights.contains(height)) {
                    heights.add(height);
                }
            }
        }

        heights.sort(Comparator.naturalOrder());
        return heights;
    }

    private Vec3 collideWithShapes(Vec3 movement, AABB box, List<VoxelShape> collisions) {
        if (collisions.isEmpty()) {
            return movement;
        }

        Vec3 resolved = Vec3.ZERO;
        for (Direction.Axis axis : Direction.axisStepOrder(movement)) {
            double axisMovement = movement.get(axis);
            if (axisMovement != 0) {
                double collision = Shapes.collide(axis, box.move(resolved), collisions, axisMovement);
                resolved = resolved.with(axis, collision);
            }
        }

        return resolved;
    }

    private boolean isEntityCollidingWithAnythingNew(AABB oldBox, Vec3 target) {
        assert mc.level != null;
        assert mc.player != null;

        AABB newBox = mc.player.getBoundingBox().move(target.x - mc.player.getX(), target.y - mc.player.getY(), target.z - mc.player.getZ());
        Iterable<VoxelShape> newCollisions = mc.level.getPreMoveCollisions(mc.player, newBox.deflate(1.0E-5F), oldBox.getBottomCenter());
        VoxelShape oldShape = Shapes.create(oldBox.deflate(1.0E-5F));

        for (VoxelShape shape : newCollisions) {
            if (!Shapes.joinIsNotEmpty(shape, oldShape, BooleanOp.AND)) {
                return true;
            }
        }

        return false;
    }

    // logic copied from Entity.checkSupportingBlock
    private boolean willBeOnGround(Vec3 position) {
        assert mc.level != null;
        assert mc.player != null;

        AABB bb = mc.player.getDimensions(mc.player.getPose()).makeBoundingBox(position);
        bb = new AABB(bb.minX, bb.minY - 1.0E-6, bb.minZ, bb.maxX, bb.minY, bb.maxZ);
        Optional<BlockPos> optional = mc.level.findSupportingBlock(mc.player, bb);
        return optional.isPresent();
    }

    @FunctionalInterface
    private interface PacketsCountCalculator {
        int calculate(double teleportDistance);
    }
}