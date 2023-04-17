package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WorldMarkersConfig extends ModuleConfig implements ValidatableConfig {

    public List<Entry> entries = new ArrayList<>();
    public int fontSize;
    public boolean antiAliasing;
    public int borderWidth;

    public WorldMarkersConfig() {
        fontSize = 12;
        borderWidth = 1;
    }

    @Override
    public void validate() {
        fontSize = MathUtils.clamp(fontSize, 8, 100);
        borderWidth = MathUtils.clamp(borderWidth, 1, 10);
        for (Entry entry : entries) {
            entry.validate();
        }
    }

    public static class Entry {
        public double x;
        public double y;
        public double z;
        public double minDistance;
        public String dimension;
        public Color color;
        public String name;
        public boolean enabled;

        public void validate() {
            minDistance = MathUtils.clamp(minDistance, 0, 1e6);
        }
    }
}