package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class ElytraHackConfig extends ModuleConfig implements ValidatableConfig {

    public static final String VANILLA_FLY = "VanillaFly";
    public static final String CREATIVE_FLY = "CreativeFly";

    public String method;

    public double vanillaFlyVerticalAcceleration;
    public double vanillaFlyHorizontalAcceleration;

    public double maxSpeed;

    public ElytraHackConfig() {
        enabled = false;
        method = VANILLA_FLY;

        maxSpeed = 50;

        vanillaFlyVerticalAcceleration = 4;
        vanillaFlyHorizontalAcceleration = 1;
    }

    @Override
    public void validate() {
        maxSpeed = MathUtils.clamp(maxSpeed, 1, 1000);

        vanillaFlyVerticalAcceleration = MathUtils.clamp(vanillaFlyVerticalAcceleration, 0, 50);
        vanillaFlyHorizontalAcceleration = MathUtils.clamp(vanillaFlyHorizontalAcceleration, 0, 50);
    }
}