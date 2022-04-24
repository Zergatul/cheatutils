package com.zergatul.cheatutils.configs;

import java.awt.*;

public abstract class JsonTracerConfigBase {

    public boolean enabled;

    public boolean drawTracers;
    public int tracerColor;
    public int tracerLineWidth;
    public int tracerLineStyle;

    public boolean drawOutline;
    public int outlineColor;
    public float outlineLineWidth;

    public double maxDistance;

    protected void convert(TracerConfigBase config) {

        config.enabled = enabled;

        config.drawTracers = drawTracers;
        config.tracerColor = new Color(tracerColor, true);
        config.tracerLineWidth = tracerLineWidth;
        config.tracerLineStyle = tracerLineStyle;

        config.drawOutline = drawOutline;
        config.outlineColor = new Color(outlineColor, true);
        config.outlineLineWidth = outlineLineWidth;

        config.maxDistance = maxDistance;
    }

}
