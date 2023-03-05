package com.zergatul.cheatutils.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VoxelShapeUtils {

    public static boolean intersects(BlockPos pos, VoxelShape shape, AABB box) {
        BooleanResult result = new BooleanResult();
        shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            if (result.value) {
                return;
            }
            AABB shapeBox = new AABB(
                    pos.getX() + x1,
                    pos.getY() + y1,
                    pos.getZ() + z1,
                    pos.getX() + x2,
                    pos.getY() + y2,
                    pos.getZ() + z2);
            if (shapeBox.intersects(box)) {
                result.value = true;
            }
        });
        return result.value;
    }

    private static class BooleanResult {
        public boolean value;
    }
}