package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WorldMarkersConfig extends ModuleConfig implements Sanitizable {

    public List<Entry> entries = new ArrayList<>();
    public FontConfig font;
    public int borderWidth;

    public WorldMarkersConfig() {
        font = new FontConfig();
        borderWidth = 1;
    }

    @Override
    public void sanitize() {
        if (font == null) {
            font = new FontConfig();
        }
        font.sanitize();

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