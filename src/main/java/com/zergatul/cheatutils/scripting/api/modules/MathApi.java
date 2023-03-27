package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.scripting.api.HelpText;
import net.minecraft.util.math.MathHelper;

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
        return MathHelper.floor(value);
    }

    public int ceil(double value) {
        return MathHelper.ceil(value);
    }

    public static class RadiansApi {

        public double sin(double value) {
            return Math.sin(value);
        }

        public double cos(double value) {
            return Math.cos(value);
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

        public double toRadians(double value) {
            return Math.toRadians(value);
        }
    }
}