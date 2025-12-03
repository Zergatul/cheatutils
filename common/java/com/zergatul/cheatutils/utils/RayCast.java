package com.zergatul.cheatutils.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.*;
import org.jspecify.annotations.Nullable;

public class RayCast {

    private static final Minecraft mc = Minecraft.getInstance();

    @Nullable
    public static RayCastResult findClosestEntity(Vec3 origin, Vec3 dir, double range) {
        if (mc.level == null) {
            return null;
        }

        dir = dir.normalize();
        if (dir.lengthSqr() < 1e-12) {
            return null;
        }

        double rangeSqr = range * range;

        Entity bestOnRayEntity = null;
        Vec3 bestOnRayHitPos = null;
        double bestOnRayT = Double.POSITIVE_INFINITY;

        Entity bestOffRayEntity = null;
        Vec3 bestOffRayHitPos = null;
        double bestOffRayScore = Double.NEGATIVE_INFINITY;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player) {
                continue;
            }
            if (!entity.isAlive()) {
                continue;
            }
            if (entity.isSpectator()) {
                continue;
            }
            if (!entity.isPickable()) {
                continue;
            }
            if (entity.distanceToSqr(origin) > rangeSqr) {
                // not very accurate, but ok
                continue;
            }

            AABB box = entity.getBoundingBox();

            // --- 1) Check if current ray hits the box directly ---
            Double tHit = intersectRayAabb(origin, dir, range, box);
            if (tHit != null) {
                if (tHit < bestOnRayT) {
                    bestOnRayT = tHit;
                    bestOnRayEntity = entity;
                    bestOnRayHitPos = origin.add(dir.scale(tHit));
                }
                // No need to evaluate angle; direct hit with some t is always better
                // than any off-ray candidate by design.
                continue;
            }

            // --- 2) No direct hit: compute minimal-angle “aim point” on AABB ---

            // Find a point in / on the box that is as close as possible to the current ray.
            Vec3 closestInBox = getClosestPointOnBoxToRay(origin, dir, range, box);

            Vec3 toAim = closestInBox.subtract(origin);
            double distToAim = toAim.length();
            if (distToAim < 1e-6 || distToAim > range) {
                continue;
            }

            Vec3 aimDir = toAim.scale(1.0 / distToAim);

            // cos(theta) between current direction and required direction
            double cosTheta = aimDir.dot(dir);
            if (cosTheta <= 0.0) {
                // Behind or exactly perpendicular – ignore
                continue;
            }

            // Higher score = smaller angle, and a tiny preference for closer targets.
            double score = cosTheta + (0.01 / distToAim);

            if (score > bestOffRayScore) {
                bestOffRayScore = score;
                bestOffRayEntity = entity;

                // For the hit point on the surface, intersect the *new* ray with the AABB.
                Double tHitRotated = intersectRayAabb(origin, aimDir, range, box);
                if (tHitRotated != null) {
                    bestOffRayHitPos = origin.add(aimDir.scale(tHitRotated));
                } else {
                    // Should normally not happen, but fallback to the closest point we computed.
                    bestOffRayHitPos = closestInBox;
                }
            }
        }

        if (bestOnRayEntity != null) {
            return new RayCastResult(bestOnRayEntity, bestOnRayHitPos);
        } else if (bestOffRayEntity != null) {
            return new RayCastResult(bestOffRayEntity, bestOffRayHitPos);
        } else {
            return null;
        }
    }

    private static Double intersectRayAabb(Vec3 origin, Vec3 dir, double maxDist, AABB box) {
        double tMin = 0.0;
        double tMax = maxDist;

        if (!axisIntersect(origin.x, dir.x, box.minX, box.maxX, tMin, tMax)) {
            return null;
        }
        tMin = lastTMin;
        tMax = lastTMax;

        if (!axisIntersect(origin.y, dir.y, box.minY, box.maxY, tMin, tMax)) {
            return null;
        }
        tMin = lastTMin;
        tMax = lastTMax;

        if (!axisIntersect(origin.z, dir.z, box.minZ, box.maxZ, tMin, tMax)) {
            return null;
        }
        tMin = lastTMin;
        tMax = lastTMax;

        if (tMax < 0.0) {
            return null;
        }

        double tHit = tMin >= 0.0 ? tMin : tMax;
        if (tHit < 0.0 || tHit > maxDist) {
            return null;
        }

        return tHit;
    }

    private static double lastTMin;
    private static double lastTMax;

    private static boolean axisIntersect(
            double origin, double dir,
            double min, double max,
            double tMinIn, double tMaxIn
    ) {
        double tMin = tMinIn;
        double tMax = tMaxIn;

        if (Math.abs(dir) < 1e-12) {
            // Ray parallel to axis – must be inside the slab
            if (origin < min || origin > max) {
                return false;
            }
        } else {
            double invD = 1.0 / dir;
            double t1 = (min - origin) * invD;
            double t2 = (max - origin) * invD;
            if (t1 > t2) {
                double tmp = t1;
                t1 = t2;
                t2 = tmp;
            }
            if (t1 > tMin) tMin = t1;
            if (t2 < tMax) tMax = t2;
            if (tMax < tMin) {
                return false;
            }
        }

        lastTMin = tMin;
        lastTMax = tMax;
        return true;
    }

    private static Vec3 getClosestPointOnBoxToRay(Vec3 origin, Vec3 dir, double range, AABB box) {
        // approximate: project center onto ray, clamp to [0, range],
        // then clamp that point into the box.
        Vec3 center = box.getCenter();

        Vec3 toCenter = center.subtract(origin);
        double product = toCenter.dot(dir);
        if (product < 0.0) product = 0.0;
        if (product > range) product = range;

        Vec3 pointOnRay = origin.add(dir.scale(product));

        double x = MathUtils.clamp(pointOnRay.x, box.minX, box.maxX);
        double y = MathUtils.clamp(pointOnRay.y, box.minY, box.maxY);
        double z = MathUtils.clamp(pointOnRay.z, box.minZ, box.maxZ);

        return new Vec3(x, y, z);
    }

    private static double clamp(double v, double min, double max) {
        return v < min ? min : (v > max ? max : v);
    }
}