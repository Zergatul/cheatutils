package com.zergatul.cheatutils.configs;

public class JsonEntityTracerConfig extends JsonTracerConfigBase {

    public String className;
    public boolean glow;

    public EntityTracerConfig convert() {
        EntityTracerConfig config = new EntityTracerConfig();
        try {
            config.clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        config.glow = glow;
        super.convert(config);
        return config;
    }
}
