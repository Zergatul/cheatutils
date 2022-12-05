package com.zergatul.cheatutils.configs;

import java.awt.*;

public class TracerConfigBase {

    protected static final double DefaultMaxDistance = 1000;

    public boolean enabled;

    public boolean drawTracers;
    public Color tracerColor;
    public int tracerLineWidth;
    public int tracerLineStyle;

    public boolean drawOutline;
    public Color outlineColor;
    public float outlineLineWidth;

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

    protected void copyFromJsonTracerConfigBase(TracerConfigBase jsonConfig) {

        enabled = jsonConfig.enabled;

        drawTracers = jsonConfig.drawTracers;
        tracerColor = jsonConfig.tracerColor;
        tracerLineWidth = jsonConfig.tracerLineWidth;
        tracerLineStyle = jsonConfig.tracerLineStyle;

        drawOutline = jsonConfig.drawOutline;
        outlineColor = jsonConfig.outlineColor;
        outlineLineWidth = jsonConfig.outlineLineWidth;

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
