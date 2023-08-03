package com.zergatul.cheatutils.utils;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class RotationUtils {

    public static Rotation getRotation(Vec3 source, Vec3 target) {
        Vec3 diff = target.subtract(source);
        double diffXZ = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float xRot = (float)Math.toDegrees(-Math.atan2(diff.y, diffXZ));
        float yRot = (float)Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90F;
        xRot = Mth.clamp(xRot, -90, 90);
        return new Rotation(xRot, yRot);
    }
}