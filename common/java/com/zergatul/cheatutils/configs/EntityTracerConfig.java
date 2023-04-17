package com.zergatul.cheatutils.configs;

import net.minecraft.world.entity.Entity;

import java.awt.*;

public class EntityTracerConfig extends TracerConfigBase {

    public Class clazz;
    public boolean glow;
    public Color glowColor;
    public Double glowMaxDistance;

    // Entity Title
    public boolean drawTitles;
    public boolean showDefaultNames;
    public boolean showHp;
    public boolean showEquippedItems;
    public boolean showOwner;

    public boolean isValidEntity(Entity entity) {
        return clazz.isInstance(entity);
    }

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

        drawTitles = jsonConfig.drawTitles;
        showDefaultNames = jsonConfig.showDefaultNames;
        showHp = jsonConfig.showHp;
        showEquippedItems = jsonConfig.showEquippedItems;
        showOwner = jsonConfig.showOwner;
    }

    public static EntityTracerConfig createDefault(Class clazz) {
        EntityTracerConfig config = new EntityTracerConfig();
        config.clazz = clazz;
        config.enabled = false;
        config.drawTracers = true;
        config.tracerColor = Color.WHITE;
        config.drawOutline = true;
        config.outlineColor = Color.WHITE;
        config.maxDistance = DefaultMaxDistance;
        config.glow = true;
        config.glowColor = Color.WHITE;
        return config;
    }
}
