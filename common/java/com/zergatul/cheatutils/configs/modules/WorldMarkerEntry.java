package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.utils.MathUtils;

import java.awt.*;

public record WorldMarkerEntry(
        double x,
        double y,
        double z,
        double minDistance,
        String dimension,
        Color color,
        String name,
        boolean enabled
) {

    public WorldMarkerEntry {
        minDistance = MathUtils.clamp(minDistance, 0, 1e6);
    }

    public static WorldMarkerEntry defaults() {
        return new Builder()
                .x(0)
                .y(0)
                .z(0)
                .minDistance(0)
                .dimension("minecraft:overworld")
                .color(Color.WHITE)
                .name("Default")
                .enabled(false)
                .build();
    }

    // <generated-code>

    public WorldMarkerEntry withX(double x) {
        return Builder.from(this).x(x).build();
    }

    public WorldMarkerEntry withY(double y) {
        return Builder.from(this).y(y).build();
    }

    public WorldMarkerEntry withZ(double z) {
        return Builder.from(this).z(z).build();
    }

    public WorldMarkerEntry withMinDistance(double minDistance) {
        return Builder.from(this).minDistance(minDistance).build();
    }

    public WorldMarkerEntry withDimension(String dimension) {
        return Builder.from(this).dimension(dimension).build();
    }

    public WorldMarkerEntry withColor(Color color) {
        return Builder.from(this).color(color).build();
    }

    public WorldMarkerEntry withName(String name) {
        return Builder.from(this).name(name).build();
    }

    public WorldMarkerEntry withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public static class Builder {

        private double x;
        private double y;
        private double z;
        private double minDistance;
        private String dimension;
        private Color color;
        private String name;
        private boolean enabled;

        private Builder() {}

        public static Builder from(WorldMarkerEntry config) {
            return new Builder()
                    .x(config.x)
                    .y(config.y)
                    .z(config.z)
                    .minDistance(config.minDistance)
                    .dimension(config.dimension)
                    .color(config.color)
                    .name(config.name)
                    .enabled(config.enabled);
        }

        public Builder x(double x) {
            this.x = x;
            return this;
        }

        public Builder y(double y) {
            this.y = y;
            return this;
        }

        public Builder z(double z) {
            this.z = z;
            return this;
        }

        public Builder minDistance(double minDistance) {
            this.minDistance = minDistance;
            return this;
        }

        public Builder dimension(String dimension) {
            this.dimension = dimension;
            return this;
        }

        public Builder color(Color color) {
            this.color = color;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public WorldMarkerEntry build() {
            return new WorldMarkerEntry(x, y, z, minDistance, dimension, color, name, enabled);
        }
    }

    // </generated-code>
}