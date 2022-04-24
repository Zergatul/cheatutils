package com.zergatul.cheatutils.configs;

import java.awt.*;

public class EntityTracerConfig extends TracerConfigBase {

    public Class clazz;
    public boolean glow;

    public JsonEntityTracerConfig convert() {
        JsonEntityTracerConfig config = new JsonEntityTracerConfig();
        config.className = clazz.getName();
        config.glow = glow;
        super.convert(config);
        return config;
    }

    public void copyFrom(JsonEntityTracerConfig jsonConfig) {
        copyFromJsonTracerConfigBase(jsonConfig);
        glow = jsonConfig.glow;
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
        return config;
    }

}
