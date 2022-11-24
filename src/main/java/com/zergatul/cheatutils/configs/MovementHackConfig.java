package com.zergatul.cheatutils.configs;

public class MovementHackConfig {
    public boolean preserveSpeed;
    public boolean disableSlowdownOnUseItem;
    public boolean scaleInputVector;
    public double inputVectorFactor;
    public boolean disableCrouchingSlowdown;
    public boolean antiKnockback;
    public boolean antiPush;

    public MovementHackConfig() {
        inputVectorFactor = 1;
    }
}
