package com.zergatul.cheatutils.configs;

import net.minecraft.world.entity.Entity;

import java.awt.*;

public class EntityTracerConfig extends TracerConfigBase {

    public Class clazz;
    public boolean glow;
    public Color glowColor;
    public Double glowMaxDistance;
    public int outlineMethod;
    public boolean drawOverlay;
    public Color overlayColor;

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
        outlineMethod = jsonConfig.outlineMethod;
        drawOverlay = jsonConfig.drawOverlay;
        overlayColor = jsonConfig.overlayColor;

        drawTitles = jsonConfig.drawTitles;
        showDefaultNames = jsonConfig.showDefaultNames;
        showHp = jsonConfig.showHp;
        showEquippedItems = jsonConfig.showEquippedItems;
        showOwner = jsonConfig.showOwner;
    }

    public boolean useMinecraftOutline() {
        return this.enabled && this.glow && this.outlineMethod == 0;
    }

    public boolean useModOutline() {
        return this.glow && this.outlineMethod == 1;
    }

    public boolean shouldDrawOverlay() {
        return this.enabled && this.drawOverlay;
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
        config.drawOverlay = false;
        config.overlayColor = new Color(0x80FFFFFF, true);
        return config;
    }
}
