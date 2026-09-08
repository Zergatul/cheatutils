package com.zergatul.cheatutils.configs;

public class FreeCamConfig implements Sanitizable {
    public double acceleration = 50;
    public double maxSpeed = 50;
    public double slowdownFactor = 0.01;
    public boolean renderHands;
    public boolean target = true;
    public boolean spectatorFlight;
    public boolean rememberInputState;

    @Override
    public void sanitize() {
        acceleration = clamp(acceleration, 5, 500, 50);
        maxSpeed = clamp(maxSpeed, 5, 500, 50);
        slowdownFactor = clamp(slowdownFactor, 1e-9, 0.5, 0.01);
    }

    private static double clamp(double value, double min, double max, double fallback) {
        return Double.isFinite(value) ? Math.max(min, Math.min(max, value)) : fallback;
    }
}
