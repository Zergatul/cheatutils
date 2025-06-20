package com.zergatul.cheatutils.utils;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public record Rotation(float xRot, float yRot) {

    public Rotation addXRot(float delta) {
        return new Rotation(xRot + delta, yRot);
    }

    public Rotation addYRot(float delta) {
        return new Rotation(xRot, yRot + delta);
    }

    public Rotation withXRot(float xRot) {
        return new Rotation(xRot, yRot);
    }

    public boolean approximateEquals(Rotation other, float epsilon) {
        return Math.abs(xRot - other.xRot) + Math.abs(yRot - other.yRot) < epsilon;
    }

    public float distanceSqrTo(Rotation other) {
        float dxRot = xRot - other.xRot;
        float dyRot = (yRot - other.yRot) % 360;
        if (dyRot < -180) {
            dyRot += 360;
        }
        if (dyRot > 180) {
            dyRot -= 360;
        }
        return dxRot * dxRot + dyRot * dyRot;
    }

    public static Rotation findClosest(Rotation current, Rotation target, Direction direction) {
        if (target == null) {
            return null;
        }

        final float step = 2.5f;
        float xRot = current.xRot();
        float yRot = current.yRot();
        while (nearestDirection(xRot, yRot) != direction) {
            float deltaXRot = getDeltaXRot(target.xRot, xRot);
            float deltaYRot = getDeltaYRot(target.yRot, yRot);
            double len = Math.sqrt(deltaXRot * deltaXRot + deltaYRot * deltaYRot);
            double xRotFactor = Math.abs(deltaXRot) / len;
            double yRotFactor = Math.abs(deltaYRot) / len;
            xRot += (float) (Math.signum(deltaXRot) * step * xRotFactor);
            yRot += (float) (Math.signum(deltaYRot) * step * yRotFactor);
        }

        return new Rotation(xRot, yRot);
    }

    public static Direction nearestDirection(float xRotDegrees, float yRotDegrees) {
        // 1) convert to radians & invert yaw to match MC’s sign convention
        float xRad = xRotDegrees * ((float) Math.PI / 180F);
        float yRad = -yRotDegrees * ((float) Math.PI / 180F);

        // 2) decompose your look-vector
        float sinX = Mth.sin(xRad);   // vertical
        float cosX = Mth.cos(xRad);   // how much of look is horizontal
        float sinY = Mth.sin(yRad);   // east/west on the horizontal plane
        float cosY = Mth.cos(yRad);   // south/north on the horizontal plane

        // 3) signs → which cardinal per axis
        boolean east  = sinY >  0F;
        boolean up    = sinX <  0F;
        boolean south = cosY >  0F;

        // 4) magnitudes along each axis
        float magEW = east  ?  sinY : -sinY;   // how “strongly” you look east vs west
        float magUD = up    ? -sinX :  sinX;   // how strongly you look up vs down
        float magNS = south ?  cosY : -cosY;   // how strongly you look south vs north

        // 5) project the two horizontal magnitudes into the horizontal plane
        float projEW = magEW * cosX;
        float projNS = magNS * cosX;

        // 6) map signs → actual Directions
        Direction dirEW = east  ? Direction.EAST  : Direction.WEST;
        Direction dirUD = up    ? Direction.UP    : Direction.DOWN;
        Direction dirNS = south ? Direction.SOUTH : Direction.NORTH;

        // 7) compare to find the “winner”
        if (magEW > magNS) {
            // east/west is stronger than north/south
            return (magUD > projEW) ? dirUD : dirEW;
        } else {
            // north/south is as strong or stronger than east/west
            return (magUD > projNS) ? dirUD : dirNS;
        }
    }

    private static float getDeltaXRot(float xRot1, float xRot2) {
        return xRot1 - xRot2;
    }

    private static float getDeltaYRot(float yRot1, float yRot2) {
        if (Float.isNaN(yRot1) || Float.isNaN(yRot2)) {
            return 0;
        } else {
            float delta = yRot1 - yRot2;
            while (delta < -180) {
                delta += 360;
            }
            while (delta >= 180) {
                delta -= 360;
            }
            return delta;
        }
    }
}