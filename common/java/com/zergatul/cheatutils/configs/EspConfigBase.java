package com.zergatul.cheatutils.configs;

import java.awt.*;

public class EspConfigBase {

    protected static final double DefaultMaxDistance = 1000;

    public boolean enabled;

    public boolean drawTracers;
    public Color tracerColor;

    public boolean drawOutline;
    public Color outlineColor;

    public double maxDistance;
    public Double tracerMaxDistance;
    public Double outlineMaxDistance;

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

    protected void copyFromJsonTracerConfigBase(EspConfigBase jsonConfig) {

        enabled = jsonConfig.enabled;

        drawTracers = jsonConfig.drawTracers;
        tracerColor = jsonConfig.tracerColor;

        drawOutline = jsonConfig.drawOutline;
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