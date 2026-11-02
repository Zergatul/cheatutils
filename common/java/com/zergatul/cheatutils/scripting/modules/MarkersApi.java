package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.modules.WorldMarkerEntry;
import com.zergatul.cheatutils.modules.visuals.WorldMarkers;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.scripting.MethodDescription;
import net.minecraft.client.Minecraft;

import java.awt.*;

public class MarkersApi {

    private final Minecraft mc = Minecraft.getInstance();

    @MethodDescription("Adds or replaces a marker in the current dimension. It is hidden within minDistance blocks. Color format: #RRGGBB or #RRGGBBAA.")
    public void add(String name, double x, double y, double z, double minDistance, String color) {
        if (mc.level == null) {
            return;
        }

        Color colorValue = ColorUtils.parseColor2(color);
        if (colorValue == null) {
            return;
        }

        WorldMarkers.INSTANCE.getSettings().update(config -> {
            ImmutableList<WorldMarkerEntry> entries = config.entries().removeAll(e -> e.name().equals(name));
            WorldMarkerEntry entry = WorldMarkerEntry.Builder.from(WorldMarkerEntry.defaults())
                    .name(name)
                    .dimension(mc.level.dimension().identifier().toString())
                    .x(x)
                    .y(y)
                    .z(z)
                    .minDistance(minDistance)
                    .color(colorValue)
                    .enabled(true)
                    .build();

            return config.withEntries(entries.add(entry));
        });
    }

    public void removeByName(String name) {
        WorldMarkers.INSTANCE.getSettings().update(config -> {
            return config.withEntries(config.entries().removeAll(e -> e.name().equals(name)));
        });
    }

    public boolean isEnabled(String name) {
        return WorldMarkers.INSTANCE.getSettings().get()
                .entries()
                .stream()
                .filter(e -> e.name().equals(name))
                .findFirst()
                .map(WorldMarkerEntry::enabled)
                .orElse(false);
    }

    public void toggle(String name) {
        WorldMarkers.INSTANCE.getSettings().update(config -> {
            return config.withEntries(
                    config.entries().replaceFirst(
                            e -> e.name().equals(name),
                            e -> e.withEnabled(!e.enabled())));
        });
    }
}