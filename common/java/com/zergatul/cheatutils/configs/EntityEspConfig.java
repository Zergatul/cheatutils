package com.zergatul.cheatutils.configs;

import net.minecraft.world.entity.Entity;

import java.awt.*;

public class EntityEspConfig extends EspConfigBase {

    public Class<?> clazz;
    public boolean drawOutline;
    public Color outlineColor;
    public Double outlineMaxDistance;
    public int outlineMethod;

    // Entity Title
    public boolean drawTitles;
    public boolean showDefaultNames;
    public boolean useRawNames;
    public boolean showHp;
    public boolean showEquippedItems;
    public boolean showOwner;

    public boolean scriptEnabled;
    public String code;

    public boolean isValidEntity(Entity entity) {
        return clazz.isInstance(entity);
    }

    public double getOutlineMaxDistanceSqr() {
        if (outlineMaxDistance != null) {
            return outlineMaxDistance * outlineMaxDistance;
        } else {
            return maxDistance * maxDistance;
        }
    }

    public void copyFrom(EntityEspConfig jsonConfig) {
        copyFromJsonTracerConfigBase(jsonConfig);
        drawOutline = jsonConfig.drawOutline;
        outlineColor = jsonConfig.outlineColor;
        outlineMaxDistance = jsonConfig.outlineMaxDistance;
        outlineMethod = jsonConfig.outlineMethod;

        drawTitles = jsonConfig.drawTitles;
        showDefaultNames = jsonConfig.showDefaultNames;
        useRawNames = jsonConfig.useRawNames;
        showHp = jsonConfig.showHp;
        showEquippedItems = jsonConfig.showEquippedItems;
        showOwner = jsonConfig.showOwner;

        scriptEnabled = jsonConfig.scriptEnabled;
    }

    public boolean useMinecraftOutline() {
        return this.enabled && this.drawOutline && this.outlineMethod == 0;
    }

    public boolean useModOutline() {
        return this.drawOutline && this.outlineMethod == 1;
    }

    public boolean shouldDrawOverlay() {
        return this.enabled && this.drawOverlay;
    }

    public static EntityEspConfig createDefault(Class<?> clazz) {
        EntityEspConfig config = new EntityEspConfig();
        config.clazz = clazz;
        config.enabled = false;
        config.drawTracers = true;
        config.tracerColor = Color.WHITE;
        config.drawBoundingBox = true;
        config.boundingBoxColor = Color.WHITE;
        config.maxDistance = DefaultMaxDistance;
        config.drawOutline = true;
        config.outlineColor = Color.WHITE;
        config.drawOverlay = false;
        config.overlayColor = new Color(0x80FFFFFF, true);
        return config;
    }
}