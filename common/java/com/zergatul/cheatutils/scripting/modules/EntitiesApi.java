package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.modules.EntityEspEntry;
import com.zergatul.cheatutils.modules.esp.entity.EntityEsp;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.scripting.MethodDescription;

import java.awt.*;

@SuppressWarnings("unused")
public class EntitiesApi {

    @MethodDescription("""
            Checks if Entity ESP rendering is enabled
            """)
    public boolean isEnabled() {
        return EntityEsp.INSTANCE.isEnabled();
    }

    @MethodDescription("""
            Toggles Entity ESP rendering
            """)
    @ApiVisibility(ApiType.UPDATE)
    public void toggle() {
        EntityEsp.INSTANCE.toggle();
    }

    @MethodDescription("""
            Returns all entity class names you have configured for Entity ESP. Does not skip disabled ones.
            """)
    public String[] getEntries() {
        return EntityEsp.INSTANCE.getSettings().get().entries().stream()
                .map(c -> c.type().getName())
                .toArray(String[]::new);
    }

    @MethodDescription("""
            Checks if Entity ESP is enabled for specified entity class
            """)
    public boolean isEnabled(String className) {
        ImmutableList<EntityEspEntry> entries = EntityEsp.INSTANCE.getSettings().get().entries();
        EntityEspEntry entry = entries.stream()
                .filter(e -> e.type().getName().equals(className))
                .findFirst()
                .orElse(null);
        if (entry == null) {
            return false;
        }
        return entry.enabled();
    }

    @MethodDescription("""
            Toggles enabled state for specified entity class
            """)
    @ApiVisibility(ApiType.UPDATE)
    public void toggle(String className) {
        EntityEsp.INSTANCE.getSettings().update(config -> {
            ImmutableList<EntityEspEntry> entries = config.entries();
            entries = entries.replaceFirst(
                    e -> e.type().getName().equals(className),
                    e -> e.withEnabled(!e.enabled()));
            return config.withEntries(entries);
        });
    }

    @MethodDescription("Color format: #RRGGBB or #RRGGBBAA.")
    @ApiVisibility(ApiType.UPDATE)
    public void setTracerColor(String className, String color) {
        Color colorValue = ColorUtils.parseColor2(color);
        if (colorValue == null) {
            return;
        }

        EntityEsp.INSTANCE.getSettings().update(config -> {
            ImmutableList<EntityEspEntry> entries = config.entries();
            entries = entries.replaceFirst(
                    e -> e.type().getName().equals(className),
                    e -> e.withTracerColor(colorValue));
            return config.withEntries(entries);
        });
    }

    @MethodDescription("Color format: #RRGGBB or #RRGGBBAA.")
    @ApiVisibility(ApiType.UPDATE)
    public void setOutlineColor(String className, String color) {
        Color colorValue = ColorUtils.parseColor2(color);
        if (colorValue == null) {
            return;
        }

        EntityEsp.INSTANCE.getSettings().update(config -> {
            ImmutableList<EntityEspEntry> entries = config.entries();
            entries = entries.replaceFirst(
                    e -> e.type().getName().equals(className),
                    e -> e.withOutlineColor(colorValue));
            return config.withEntries(entries);
        });
    }

    @MethodDescription("Color format: #RRGGBB or #RRGGBBAA.")
    @ApiVisibility(ApiType.UPDATE)
    public void setCollisionBoxColor(String className, String color) {
        Color colorValue = ColorUtils.parseColor2(color);
        if (colorValue == null) {
            return;
        }

        EntityEsp.INSTANCE.getSettings().update(config -> {
            ImmutableList<EntityEspEntry> entries = config.entries();
            entries = entries.replaceFirst(
                    e -> e.type().getName().equals(className),
                    e -> e.withBoundingBoxColor(colorValue));
            return config.withEntries(entries);
        });
    }

    @MethodDescription("Color format: #RRGGBB or #RRGGBBAA.")
    @ApiVisibility(ApiType.UPDATE)
    public void setOverlayColor(String className, String color) {
        Color colorValue = ColorUtils.parseColor2(color);
        if (colorValue == null) {
            return;
        }

        EntityEsp.INSTANCE.getSettings().update(config -> {
            ImmutableList<EntityEspEntry> entries = config.entries();
            entries = entries.replaceFirst(
                    e -> e.type().getName().equals(className),
                    e -> e.withOverlayColor(colorValue));
            return config.withEntries(entries);
        });
    }
}