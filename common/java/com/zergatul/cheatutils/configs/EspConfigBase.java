package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

import java.awt.*;

public abstract class EspConfigBase {

    protected static final double DefaultMaxDistance = 1000;

    public boolean enabled;

    public boolean drawTracers;
    public double tracerWidth;
    public Color tracerColor;

    public boolean drawBoundingBox;
    public double boundingBoxWidth;
    public Color boundingBoxColor;

    public boolean drawOverlay;
    public Color overlayColor;

    public double maxDistance;
    public Double tracerMaxDistance;
    public Double boundingBoxMaxDistance;

    protected EspConfigBase() {
        tracerWidth = 1;
        boundingBoxWidth = 1;
    }

    public double getTracerMaxDistanceSqr() {
        if (tracerMaxDistance != null) {
            return tracerMaxDistance * tracerMaxDistance;
        } else {
            return maxDistance * maxDistance;
        }
    }

    public double getBoundingBoxMaxDistanceSqr() {
        if (boundingBoxMaxDistance != null) {
            return boundingBoxMaxDistance * boundingBoxMaxDistance;
        } else {
            return maxDistance * maxDistance;
        }
    }

    public double getOverlayMaxDistanceSqr() {
        return maxDistance * maxDistance;
    }

    public void validate() {
        tracerWidth = MathUtils.clamp(tracerWidth, 0.5, 100);
        boundingBoxWidth = MathUtils.clamp(boundingBoxWidth, 0.5, 100);
    }

    protected void copyFromJsonTracerConfigBase(EspConfigBase jsonConfig) {

        enabled = jsonConfig.enabled;

        drawTracers = jsonConfig.drawTracers;
        tracerWidth = jsonConfig.tracerWidth;
        tracerColor = jsonConfig.tracerColor;

        drawBoundingBox = jsonConfig.drawBoundingBox;
        boundingBoxWidth = jsonConfig.boundingBoxWidth;
        boundingBoxColor = jsonConfig.boundingBoxColor;

        drawOverlay = jsonConfig.drawOverlay;
        overlayColor = jsonConfig.overlayColor;

        maxDistance = jsonConfig.maxDistance;
        if (Double.isNaN(maxDistance)) {
            maxDistance = DefaultMaxDistance;
        }
        if (maxDistance <= 0) {
            maxDistance = DefaultMaxDistance;
        }

        tracerMaxDistance = jsonConfig.tracerMaxDistance;
        boundingBoxMaxDistance = jsonConfig.boundingBoxMaxDistance;
    }
}