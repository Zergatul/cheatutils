package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class MovementHackConfig implements ValidatableConfig {
    public boolean disableSlowdownOnUseItem;
    public boolean scaleInputVector;
    public double inputVectorFactor;
    public boolean disableCrouchingSlowdown;
    public boolean antiKnockback;
    public boolean antiPush;
    public boolean scaleJumpHeight;
    public double jumpHeightFactor;
    public boolean disableSlimePhysics;
    public boolean disableWaterPush;

    public MovementHackConfig() {
        inputVectorFactor = 1;
        jumpHeightFactor = 1;
    }

    public void validate() {
        inputVectorFactor = MathUtils.clamp(inputVectorFactor, 0.01, 1000);
        jumpHeightFactor = MathUtils.clamp(jumpHeightFactor, 0.01, 1000);
    }
}