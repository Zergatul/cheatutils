package com.zergatul.cheatutils.configs;

public class FreeCamConfig {
    public double acceleration;
    public double maxSpeed;
    public double slowdownFactor;
    public boolean renderHands;

    public FreeCamConfig() {
        acceleration = 50;
        maxSpeed = 50;
        slowdownFactor = 0.01;
    }
}