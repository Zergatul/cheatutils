package com.zergatul.cheatutils.configs.modules;

public record SchematicaWorldSlice(
        boolean enabled,
        int x1,
        int y1,
        int z1,
        int x2,
        int y2,
        int z2
) {
    public static SchematicaWorldSlice defaults() {
        return new SchematicaWorldSlice(false, 0, 0, 0, 0, 0, 0);
    }

    public int getX1() {
        return Math.min(x1, x2);
    }

    public int getX2() {
        return Math.max(x1, x2) + 1;
    }

    public int getY1() {
        return Math.min(y1, y2);
    }

    public int getY2() {
        return Math.max(y1, y2) + 1;
    }

    public int getZ1() {
        return Math.min(z1, z2);
    }

    public int getZ2() {
        return Math.max(z1, z2) + 1;
    }
}