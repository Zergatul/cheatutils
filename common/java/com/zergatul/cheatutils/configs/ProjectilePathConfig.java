package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class ProjectilePathConfig extends ModuleConfig implements Sanitizable {

    public boolean enderPearls;
    public boolean bows;
    public boolean crossbows;
    public boolean tridents;
    public boolean snowballs;
    public boolean potions;
    public boolean expBottles;
    public boolean eggs;

    public boolean showTraces;
    public int tracesDuration;
    public int fadeDuration;

    public ProjectilePathConfig() {
        enabled = true;

        enderPearls = true;

        tracesDuration = 10;
        fadeDuration = 2;
    }

    @Override
    public void sanitize() {
        tracesDuration = MathUtils.clamp(tracesDuration, 0, 3600);
        fadeDuration = MathUtils.clamp(fadeDuration, 0, tracesDuration);
    }
}