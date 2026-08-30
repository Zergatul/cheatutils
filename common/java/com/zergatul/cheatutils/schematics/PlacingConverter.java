package com.zergatul.cheatutils.schematics;

import net.minecraft.core.BlockPos;
import org.joml.Matrix3f;

public class PlacingConverter {

    private final int width;
    private final int height;
    private final int length;
    private final Matrix3i transform;
    private final int[] offset;

    public PlacingConverter(PlacingSettings settings, int width, int height, int length) {
        Matrix3f transform = new Matrix3f(IDENTITY);
        for (String str : settings.transforms) {
            Matrix3f current = switch (str) {
                case "Flip X" -> FLIP_X;
                case "Flip Y" -> FLIP_Y;
                case "Flip Z" -> FLIP_Z;
                case "Rotate X -90deg" -> ROT_X_M90;
                case "Rotate X +90deg" -> ROT_X_P90;
                case "Rotate X 180deg" -> ROT_X_180;
                case "Rotate Y -90deg" -> ROT_Y_M90;
                case "Rotate Y +90deg" -> ROT_Y_P90;
                case "Rotate Y 180deg" -> ROT_Y_180;
                case "Rotate Z -90deg" -> ROT_Z_M90;
                case "Rotate Z +90deg" -> ROT_Z_P90;
                case "Rotate Z 180deg" -> ROT_Z_180;
                case "" -> IDENTITY;
                default -> throw new IllegalStateException("Unknown transformation.");
            };
            transform.mul(current);
        }

        this.transform = new Matrix3i(transform);
        this.offset = calcOffset(transform, width, height, length);

        this.width = Math.abs(this.transform.m00) * width + Math.abs(this.transform.m10) * height + Math.abs(this.transform.m20) * length;
        this.height = Math.abs(this.transform.m01) * width + Math.abs(this.transform.m11) * height + Math.abs(this.transform.m21) * length;
        this.length = Math.abs(this.transform.m02) * width + Math.abs(this.transform.m12) * height + Math.abs(this.transform.m22) * length;
    }

    public void convert(BlockPos.MutableBlockPos vec) {
        int newX = transform.m00 * vec.getX() + transform.m10 * vec.getY() + transform.m20 * vec.getZ() + offset[0];
        int newY = transform.m01 * vec.getX() + transform.m11 * vec.getY() + transform.m21 * vec.getZ() + offset[1];
        int newZ = transform.m02 * vec.getX() + transform.m12 * vec.getY() + transform.m22 * vec.getZ() + offset[2];
        vec.set(newX, newY, newZ);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getLength() {
        return length;
    }

    private static final Matrix3f IDENTITY = new Matrix3f(
            1,  0,  0,
            0,  1,  0,
            0,  0,  1);
    private static final Matrix3f FLIP_X = new Matrix3f(
            -1,  0,  0,
            0,  1,  0,
            0,  0,  1);
    private static final Matrix3f FLIP_Y = new Matrix3f(
            1,  0,  0,
            0, -1,  0,
            0,  0,  1);
    private static final Matrix3f FLIP_Z = new Matrix3f(
            1,  0,  0,
            0,  1,  0,
            0,  0, -1);
    private static final Matrix3f ROT_X_M90 = new Matrix3f(
            1,  0,  0,
            0,  0,  1,
            0, -1,  0);
    private static final Matrix3f ROT_X_P90 = new Matrix3f(
            1,  0,  0,
            0,  0, -1,
            0,  1,  0);
    private static final Matrix3f ROT_X_180 = new Matrix3f(
            1,  0,  0,
            0, -1,  0,
            0,  0, -1);
    private static final Matrix3f ROT_Y_M90 = new Matrix3f(
            0,  0, -1,
            0,  1,  0,
            1,  0,  0);
    private static final Matrix3f ROT_Y_P90 = new Matrix3f(
            0,  0,  1,
            0,  1,  0,
            -1,  0,  0);
    private static final Matrix3f ROT_Y_180 = new Matrix3f(
            -1,  0,  0,
            0,  1,  0,
            0,  0, -1);
    private static final Matrix3f ROT_Z_M90 = new Matrix3f(
            0,  1,  0,
            -1,  0,  0,
            0,  0,  1);
    private static final Matrix3f ROT_Z_P90 = new Matrix3f(
            0, -1,  0,
            1,  0,  0,
            0,  0,  1);
    private static final Matrix3f ROT_Z_180 = new Matrix3f(
            -1,  0,  0,
            0, -1,  0,
            0,  0,  1);

    private static int[] calcOffset(Matrix3f m, int width, int height, int length) {
        int[] offset = new int[3];
        for (int i = 0; i < 3; i++) {
            if (m.get(0, i) == -1) {
                offset[i] = width - 1;
            }
            if (m.get(1, i) == -1) {
                offset[i] = height - 1;
            }
            if (m.get(2, i) == -1) {
                offset[i] = length - 1;
            }
        }
        return offset;
    }

    private static class Matrix3i {
        public int m00, m01, m02;
        public int m10, m11, m12;
        public int m20, m21, m22;

        public Matrix3i(Matrix3f matrix) {
            m00 = (int) matrix.m00;
            m01 = (int) matrix.m01;
            m02 = (int) matrix.m02;
            m10 = (int) matrix.m10;
            m11 = (int) matrix.m11;
            m12 = (int) matrix.m12;
            m20 = (int) matrix.m20;
            m21 = (int) matrix.m21;
            m22 = (int) matrix.m22;
        }
    }
}