package com.zergatul.cheatutils.configs;

import net.minecraft.world.entity.Entity;

import java.awt.Color;

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

    public boolean isValidEntity(Entity entity) {
        return clazz != null && clazz.isInstance(entity);
    }

    public double getOutlineMaxDistanceSqr() {
        double distance = outlineMaxDistance != null ? outlineMaxDistance : maxDistance;
        return distance * distance;
    }

    public boolean useMinecraftOutline() {
        return enabled && drawOutline && outlineMethod == 0;
    }

    public boolean useModOutline() {
        return enabled && drawOutline && outlineMethod == 1;
    }

    public boolean shouldDrawOverlay() {
        return enabled && drawOverlay;
    }

    public void copyFrom(EntityEspConfig jsonConfig) {
        copyFromJsonEspConfigBase(jsonConfig);

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

        sanitize();
    }

    @Override
    public void sanitize() {
        super.sanitize();

        if (outlineColor == null) {
            outlineColor = Color.WHITE;
        }
        if (outlineMaxDistance != null && (!Double.isFinite(outlineMaxDistance) || outlineMaxDistance <= 0)) {
            outlineMaxDistance = null;
        }
        if (outlineMethod < 0 || outlineMethod > 1) {
            outlineMethod = 0;
        }
    }

    public static EntityEspConfig createDefault(Class<?> clazz) {
        EntityEspConfig config = new EntityEspConfig();
        config.clazz = clazz;
        config.enabled = false;

        config.drawTracers = true;
        config.tracerColor = Color.WHITE;

        config.drawBoundingBox = true;
        config.boundingBoxColor = Color.WHITE;

        config.drawOutline = true;
        config.outlineColor = Color.WHITE;
        config.outlineMethod = 0;

        config.drawOverlay = false;
        config.overlayColor = new Color(0x80FFFFFF, true);

        config.maxDistance = DefaultMaxDistance;
        return config;
    }
}