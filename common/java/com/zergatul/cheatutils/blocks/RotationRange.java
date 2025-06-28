package com.zergatul.cheatutils.blocks;

import com.zergatul.cheatutils.utils.MathUtils;
import com.zergatul.cheatutils.utils.Rotation;

import java.util.List;

public record RotationRange(float xRot1, float xRot2, float yRot1, float yRot2) {

    public static final RotationRange ANY = new RotationRange(-90, 90, -180, 180);

    public RotationRange(float xRot1, float xRot2, float yRot1, float yRot2, float step) {
        this(xRotStep(xRot1, +step), xRotStep(xRot2, -step), yRotStep(yRot1, +step), yRotStep(yRot2, -step));
    }

    public static RotationRange x(float xRot1, float xRot2, float step) {
        return new RotationRange(xRotStep(xRot1, +step), xRotStep(xRot2, -step), -180, 180);
    }

    public static RotationRange y(float yRot1, float yRot2, float step) {
        return new RotationRange(-90, 90, yRotStep(yRot1, +step), yRotStep(yRot2, -step));
    }

    public static Rotation findClosest(Rotation current, List<RotationRange> ranges) {
        if (ranges == null || ranges.isEmpty()) {
            return null;
        }

        Rotation best = null;
        double bestDist = Double.MAX_VALUE;
        for (RotationRange range : ranges) {
            if (range.contains(current)) {
                // no need to apply rotation
                return null;
            }

            float targetXRot = MathUtils.clamp(current.xRot(), range.xRot1, range.xRot2);
            float targetYRot = MathUtils.clamp(current.yRot(), range.yRot1, range.yRot2);
            float deltaXRot = targetXRot - current.xRot();
            float deltaYRot = MathUtils.deltaAngle180(targetYRot, current.yRot());
            float distanceSqr = deltaXRot * deltaXRot + deltaYRot * deltaYRot;
            if (distanceSqr < bestDist) {
                bestDist = distanceSqr;
                best = new Rotation(targetXRot, targetYRot);
            }
        }

        return best;
    }

    public boolean contains(Rotation rotation) {
        return xRot1 <= rotation.xRot() && rotation.xRot() <= xRot2 && yRot1 <= rotation.yRot() && rotation.yRot() <= yRot2;
    }

    private static float xRotStep(float xRot, float step) {
        if (xRot == -90 || xRot == 90) {
            return xRot;
        } else {
            return xRot + step;
        }
    }

    private static float yRotStep(float yRot, float step) {
        if (yRot == -180 || yRot == 180) {
            return yRot;
        } else {
            return yRot + step;
        }
    }
}