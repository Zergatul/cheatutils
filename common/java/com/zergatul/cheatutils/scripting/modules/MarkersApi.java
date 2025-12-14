package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.WorldMarkersConfig;
import com.zergatul.cheatutils.utils.ColorUtils;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.util.Optional;

public class MarkersApi {

    private final Minecraft mc = Minecraft.getInstance();

    public void add(String name, double x, double y, double z, double minDistance, String color) {
        if (mc.level == null) {
            return;
        }

        Color colorValue = ColorUtils.parseColor2(color);
        if (colorValue == null) {
            return;
        }

        WorldMarkersConfig config = getConfig();
        config.entries.removeIf(e -> e.name.equals(name));

        WorldMarkersConfig.Entry entry = new WorldMarkersConfig.Entry();
        entry.name = name;
        entry.dimension = mc.level.dimension().location().toString();
        entry.x = x;
        entry.y = y;
        entry.z = z;
        entry.minDistance = minDistance;
        entry.color = colorValue;
        entry.enabled = true;
        entry.validate();

        config.entries.add(entry);
        ConfigStore.instance.requestWrite();
    }

    public void removeByName(String name) {
        if (getConfig().entries.removeIf(e -> e.name.equals(name))) {
            ConfigStore.instance.requestWrite();
        }
    }

    public boolean isEnabled(String name) {
        return getEntry(name).map(e -> e.enabled).orElse(false);
    }

    public void toggle(String name) {
        getEntry(name).ifPresent(e -> {
            e.enabled = !e.enabled;
            ConfigStore.instance.requestWrite();
        });
    }

    private Optional<WorldMarkersConfig.Entry> getEntry(String name) {
        return getConfig().entries.stream().filter(e -> e.name.equals(name)).findFirst();
    }

    private WorldMarkersConfig getConfig() {
        return ConfigStore.instance.getConfig().worldMarkersConfig;
    }
}