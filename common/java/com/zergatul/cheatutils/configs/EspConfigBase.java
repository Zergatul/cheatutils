package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

import java.awt.*;

public abstract class EspConfigBase {

    protected static final double DefaultMaxDistance = 1000;

    public boolean enabled;

    public boolean drawTracers;
    public int tracerWidth;
    public Color tracerColor;

    public boolean drawOutline;
    public int outlineWidth;
    public Color outlineColor;

    public double maxDistance;
    public Double tracerMaxDistance;
    public Double outlineMaxDistance;

    protected EspConfigBase() {
        tracerWidth = 1;
        outlineWidth = 1;
    }

    public double getTracerMaxDistanceSqr() {
        if (tracerMaxDistance != null) {
            return tracerMaxDistance * tracerMaxDistance;
        } else {
            return maxDistance * maxDistance;
        }
    }

    public double getOutlineMaxDistanceSqr() {
        if (outlineMaxDistance != null) {
            return outlineMaxDistance * outlineMaxDistance;
        } else {
            return maxDistance * maxDistance;
        }
    }

    public double getOverlayMaxDistanceSqr() {
        return maxDistance * maxDistance;
    }

    public void validate() {
        tracerWidth = MathUtils.clamp(tracerWidth, 1, 100);
        outlineWidth = MathUtils.clamp(outlineWidth, 1, 100);
    }

    protected void copyFromJsonTracerConfigBase(EspConfigBase jsonConfig) {

        enabled = jsonConfig.enabled;

        drawTracers = jsonConfig.drawTracers;
        tracerWidth = jsonConfig.tracerWidth;
        tracerColor = jsonConfig.tracerColor;

        drawOutline = jsonConfig.drawOutline;
        outlineWidth = jsonConfig.outlineWidth;
        outlineColor = jsonConfig.outlineColor;

        maxDistance = jsonConfig.maxDistance;
        if (Double.isNaN(maxDistance)) {
            maxDistance = DefaultMaxDistance;
        }
        if (maxDistance <= 0) {
            maxDistance = DefaultMaxDistance;
        }

        tracerMaxDistance = jsonConfig.tracerMaxDistance;
        outlineMaxDistance = jsonConfig.outlineMaxDistance;
    }
}