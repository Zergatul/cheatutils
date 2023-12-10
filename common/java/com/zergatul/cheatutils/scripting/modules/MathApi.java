package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.scripting.HelpText;
import net.minecraft.util.Mth;

import java.util.Random;

public class MathApi {

    public final RadiansApi radians = new RadiansApi();
    public final DegreesApi degrees = new DegreesApi();

    private final Random random = new Random();

    @HelpText("Random number in [0..1) range")
    public double random() {
        return random.nextDouble();
    }

    public int randomInt(int max) {
        return random.nextInt(max);
    }

    public int randomInt(int min, int max) {
        return random.nextInt(min, max);
    }

    public int round(double value) {
        return (int) Math.round(value);
    }

    public int floor(double value) {
        return Mth.floor(value);
    }

    public int ceil(double value) {
        return Mth.ceil(value);
    }

    public int abs(int value) {
        return Math.abs(value);
    }

    public double abs(double value) {
        return Math.abs(value);
    }

    public double sqrt(double value) {
        return Math.sqrt(value);
    }

    public static class RadiansApi {

        public double sin(double value) {
            return Math.sin(value);
        }

        public double cos(double value) {
            return Math.cos(value);
        }

        public double tan(double value) {
            return Math.tan(value);
        }

        public double asin(double value) {
            return Math.asin(value);
        }

        public double acos(double value) {
            return Math.asin(value);
        }

        public double atan(double value) {
            return Math.atan(value);
        }

        public double atan2(double y, double x) {
            return Math.atan2(y, x);
        }

        public double toDegrees(double value) {
            return Math.toDegrees(value);
        }
    }

    public static class DegreesApi {

        public double sin(double value) {
            return Math.sin(Math.toRadians(value));
        }

        public double cos(double value) {
            return Math.cos(Math.toRadians(value));
        }

        public double tan(double value) {
            return Math.tan(Math.toRadians(value));
        }

        public double asin(double value) {
            return Math.toDegrees(Math.asin(value));
        }

        public double acos(double value) {
            return Math.toDegrees(Math.asin(value));
        }

        public double atan(double value) {
            return Math.toDegrees(Math.atan(value));
        }

        public double atan2(double y, double x) {
            return Math.toDegrees(Math.atan2(y, x));
        }

        public double toRadians(double value) {
            return Math.toRadians(value);
        }
    }
}