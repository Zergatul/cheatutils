package com.zergatul.cheatutils.utils;

public class MathUtils {
    public static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public static float clamp(float value, float min, float max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public static double deltaAngle180(double angle1, double angle2) {
        double result = (angle1 - angle2) % 360;
        if (result < -180) {
            result += 360;
        }
        if (result >= 180) {
            result -= 360;
        }
        return Math.abs(result);
    }
}