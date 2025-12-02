package com.zergatul.cheatutils.scripting.types;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("unused")
@CustomType(name = "Position3d")
public class Position3d {

    private final double x;
    private final double y;
    private final double z;

    public Position3d(Vec3 vec) {
        this(vec.x, vec.y, vec.z);
    }

    public Position3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Getter(name = "x")
    public double getX() {
        return x;
    }

    @Getter(name = "y")
    public double getY() {
        return y;
    }

    @Getter(name = "z")
    public double getZ() {
        return z;
    }

    public double distanceTo(Position3d other) {
        double dx = other.x - x;
        double dy = other.y - y;
        double dz = other.z - z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}