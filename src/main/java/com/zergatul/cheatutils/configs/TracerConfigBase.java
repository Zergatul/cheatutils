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

    protected void convert(JsonTracerConfigBase config) {

        config.enabled = enabled;

        config.drawTracers = drawTracers;
        config.tracerColor = tracerColor.getRGB();
        config.tracerLineWidth = tracerLineWidth;
        config.tracerLineStyle = tracerLineStyle;

        config.drawOutline = drawOutline;
        config.outlineColor = outlineColor.getRGB();
        config.outlineLineWidth = outlineLineWidth;

        config.maxDistance = maxDistance;
    }

    protected void copyFromJsonTracerConfigBase(JsonTracerConfigBase jsonConfig) {

        enabled = jsonConfig.enabled;

        drawTracers = jsonConfig.drawTracers;
        tracerColor = new Color(jsonConfig.tracerColor, true);
        tracerLineWidth = jsonConfig.tracerLineWidth;
        tracerLineStyle = jsonConfig.tracerLineStyle;

        drawOutline = jsonConfig.drawOutline;
        outlineColor = new Color(jsonConfig.outlineColor, true);
        outlineLineWidth = jsonConfig.outlineLineWidth;

        maxDistance = jsonConfig.maxDistance;
        if (Double.isNaN(maxDistance)) {
            maxDistance = DefaultMaxDistance;
        }
        if (maxDistance <= 0) {
            maxDistance = DefaultMaxDistance;
        }
    }
}
