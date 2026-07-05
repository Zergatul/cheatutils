package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class MovementHackConfig implements Sanitizable, ModuleStateProvider {
    public boolean disableSlowdownOnUseItem;
    public boolean scaleInputVector;
    public double inputVectorFactor;
    public boolean disableCrouchingSlowdown;
    public boolean antiKnockback;
    public boolean antiPush;
    public boolean scaleJumpHeight;
    public double jumpHeightFactor;
    public boolean disableWaterPush;

    public MovementHackConfig() {
        inputVectorFactor = 1;
        jumpHeightFactor = 1;
    }

    public void sanitize() {
        inputVectorFactor = inputVectorFactor;
        jumpHeightFactor = jumpHeightFactor;
    }

    @Override
    public boolean isEnabled() {
        return disableSlowdownOnUseItem || scaleInputVector || disableCrouchingSlowdown || antiKnockback || antiPush || scaleJumpHeight || disableWaterPush;
    }
}