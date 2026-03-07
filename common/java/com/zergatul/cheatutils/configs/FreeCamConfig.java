package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class FreeCamConfig implements Sanitizable {
    public double acceleration;
    public double maxSpeed;
    public double slowdownFactor;
    public boolean renderHands;
    public boolean target;
    public boolean spectatorFlight;
    public boolean rememberInputState;

    public FreeCamConfig() {
        acceleration = 50;
        maxSpeed = 50;
        slowdownFactor = 0.01;
        target = true;
    }

    @Override
    public void sanitize() {
        acceleration = MathUtils.clamp(acceleration, 5, 500);
        maxSpeed = MathUtils.clamp(maxSpeed, 5, 500);
        slowdownFactor = MathUtils.clamp(slowdownFactor, 1e-9, 0.5);
    }
}