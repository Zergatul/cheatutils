package com.zergatul.cheatutils.configs;

import java.awt.*;

public class EntityTracerConfig extends TracerConfigBase {

    public Class clazz;
    public boolean glow;
    public Color glowColor;
    public Double glowMaxDistance;

    public double getGlowMaxDistanceSqr() {
        if (glowMaxDistance != null) {
            return glowMaxDistance * glowMaxDistance;
        } else {
            return maxDistance * maxDistance;
        }
    }

    public void copyFrom(EntityTracerConfig jsonConfig) {
        copyFromJsonTracerConfigBase(jsonConfig);
        glow = jsonConfig.glow;
        glowColor = jsonConfig.glowColor;
        glowMaxDistance = jsonConfig.glowMaxDistance;
    }

    public static EntityTracerConfig createDefault(Class clazz) {
        EntityTracerConfig config = new EntityTracerConfig();
        config.clazz = clazz;
        config.enabled = false;
        config.drawTracers = true;
        config.tracerColor = Color.WHITE;
        config.tracerLineWidth = 2;
        config.tracerLineStyle = 0;
        config.drawOutline = true;
        config.outlineColor = Color.WHITE;
        config.outlineLineWidth = 2;
        config.maxDistance = DefaultMaxDistance;
        config.glow = true;
        config.glowColor = Color.WHITE;
        return config;
    }

}
