package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

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

    public void validate() {
        inputVectorFactor = MathUtils.clamp(inputVectorFactor, 0.01, 1000);
    }
}