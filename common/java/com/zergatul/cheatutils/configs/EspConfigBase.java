package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

import java.awt.*;

public abstract class EspConfigBase {

    protected static final double DefaultMaxDistance = 1000;

    public boolean enabled;

    public boolean drawTracers;
    public double tracerWidth = 1;
    public Color tracerColor;

    public boolean drawBoundingBox;
    public double boundingBoxWidth = 1;
    public Color boundingBoxColor;

    public boolean drawOverlay;
    public Color overlayColor;

    public double maxDistance;
    public Double tracerMaxDistance;
    public Double boundingBoxMaxDistance;

    public double getTracerMaxDistanceSqr() {
        double distance = tracerMaxDistance != null ? tracerMaxDistance : maxDistance;
        return distance * distance;
    }

    public double getBoundingBoxMaxDistanceSqr() {
        double distance = boundingBoxMaxDistance != null ? boundingBoxMaxDistance : maxDistance;
        return distance * distance;
    }

    public double getOverlayMaxDistanceSqr() {
        return maxDistance * maxDistance;
    }

    public void sanitize() {
        tracerWidth = sanitizeWidth(tracerWidth);
        boundingBoxWidth = sanitizeWidth(boundingBoxWidth);
        maxDistance = sanitizeDistance(maxDistance, DefaultMaxDistance);
        tracerMaxDistance = sanitizeOptionalDistance(tracerMaxDistance);
        boundingBoxMaxDistance = sanitizeOptionalDistance(boundingBoxMaxDistance);

        if (tracerColor == null) {
            tracerColor = Color.WHITE;
        }
        if (boundingBoxColor == null) {
            boundingBoxColor = Color.WHITE;
        }
        if (overlayColor == null) {
            overlayColor = new Color(0x80FFFFFF, true);
        }
    }

    protected void copyFromJsonEspConfigBase(EspConfigBase jsonConfig) {
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
        tracerMaxDistance = jsonConfig.tracerMaxDistance;
        boundingBoxMaxDistance = jsonConfig.boundingBoxMaxDistance;

        sanitize();
    }

    private static double sanitizeWidth(double value) {
        return Double.isFinite(value) ? MathUtils.clamp(value, 0.5, 100) : 1;
    }

    private static double sanitizeDistance(double value, double fallback) {
        return Double.isFinite(value) && value > 0 ? value : fallback;
    }

    private static Double sanitizeOptionalDistance(Double value) {
        return value != null && Double.isFinite(value) && value > 0 ? value : null;
    }
}