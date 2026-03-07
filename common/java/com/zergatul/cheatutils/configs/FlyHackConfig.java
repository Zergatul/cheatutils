package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class FlyHackConfig extends ModuleConfig implements Sanitizable {

    public boolean overrideFlyingSpeed;
    public float flyingSpeed;
    public boolean onGroundFlag;

    public boolean vanillaAntiFlyBypass;
    public int antiFlyInterval;
    public double antiFlyDistance;

    public FlyHackConfig() {
        enabled = false;
        overrideFlyingSpeed = false;
        flyingSpeed = 0.05f;
        vanillaAntiFlyBypass = false;
        antiFlyInterval = 40;
        antiFlyDistance = 0.05;
    }

    @Override
    public void sanitize() {
        flyingSpeed = MathUtils.clamp(flyingSpeed, 0.001f, 10f);
        antiFlyInterval = MathUtils.clamp(antiFlyInterval, 2, 200);
        antiFlyDistance = MathUtils.clamp(antiFlyDistance, 0.001, 1);
    }
}