package com.zergatul.cheatutils.configs.utility;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.font.FontParameters;
import com.zergatul.cheatutils.font.FontRenderDetails;
import com.zergatul.cheatutils.font.FontRendererType;
import com.zergatul.cheatutils.utils.MathUtils;

@SuppressWarnings("unused")
public record FontConfig(
        FontRendererType renderer,
        String face,
        double size,
        int scale,
        boolean antiAliasing,
        double letterSpacing,
        double lineSpacing,
        boolean dropShadow,
        int shadowOffsetX,
        int shadowOffsetY
) implements ImmutableConfig {

    public FontConfig {
        size = MathUtils.clamp(size, 8, 100);
        letterSpacing = MathUtils.clamp(letterSpacing, -10, 10);
        lineSpacing = MathUtils.clamp(lineSpacing, -50, 50);
    }

    public static FontConfig defaults() {
        return new FontConfig(
                FontRendererType.VANILLA,
                "Consolas",
                16,
                0,
                false,
                0,
                0,
                false,
                1,
                1);
    }

    public boolean equals(FontConfig other) {
        if (other.renderer != renderer) {
            return false;
        }
        return switch (renderer) {
            case AWT ->
                    other.face.equals(face) &&
                    other.size == size &&
                    other.antiAliasing == antiAliasing &&
                    other.letterSpacing == letterSpacing &&
                    other.lineSpacing == lineSpacing &&
                    other.dropShadow == dropShadow &&
                    other.shadowOffsetX == shadowOffsetX &&
                    other.shadowOffsetY == shadowOffsetY;
            case STB ->
                    other.face.equals(face) &&
                    other.size == size &&
                    other.letterSpacing == letterSpacing &&
                    other.lineSpacing == lineSpacing &&
                    other.dropShadow == dropShadow &&
                    other.shadowOffsetX == shadowOffsetX &&
                    other.shadowOffsetY == shadowOffsetY;
            case VANILLA ->
                    other.scale == scale &&
                    other.dropShadow == dropShadow;
        };
    }

    public FontParameters asFontParameters() {
        return new FontParameters(renderer, face, (float) size, antiAliasing);
    }

    public FontRenderDetails asFontRenderDetails() {
        return new FontRenderDetails((float) letterSpacing, (float) lineSpacing, dropShadow, shadowOffsetX, shadowOffsetY, scale);
    }

    // <generated-code>

    public FontConfig withRenderer(FontRendererType renderer) {
        return Builder.from(this).renderer(renderer).build();
    }

    public FontConfig withFace(String face) {
        return Builder.from(this).face(face).build();
    }

    public FontConfig withSize(double size) {
        return Builder.from(this).size(size).build();
    }

    public FontConfig withScale(int scale) {
        return Builder.from(this).scale(scale).build();
    }

    public FontConfig withAntiAliasing(boolean antiAliasing) {
        return Builder.from(this).antiAliasing(antiAliasing).build();
    }

    public FontConfig withLetterSpacing(double letterSpacing) {
        return Builder.from(this).letterSpacing(letterSpacing).build();
    }

    public FontConfig withLineSpacing(double lineSpacing) {
        return Builder.from(this).lineSpacing(lineSpacing).build();
    }

    public FontConfig withDropShadow(boolean dropShadow) {
        return Builder.from(this).dropShadow(dropShadow).build();
    }

    public FontConfig withShadowOffsetX(int shadowOffsetX) {
        return Builder.from(this).shadowOffsetX(shadowOffsetX).build();
    }

    public FontConfig withShadowOffsetY(int shadowOffsetY) {
        return Builder.from(this).shadowOffsetY(shadowOffsetY).build();
    }

    public static class Builder {

        private FontRendererType renderer;
        private String face;
        private double size;
        private int scale;
        private boolean antiAliasing;
        private double letterSpacing;
        private double lineSpacing;
        private boolean dropShadow;
        private int shadowOffsetX;
        private int shadowOffsetY;

        private Builder() {}

        public static Builder from(FontConfig config) {
            return new Builder()
                    .renderer(config.renderer)
                    .face(config.face)
                    .size(config.size)
                    .scale(config.scale)
                    .antiAliasing(config.antiAliasing)
                    .letterSpacing(config.letterSpacing)
                    .lineSpacing(config.lineSpacing)
                    .dropShadow(config.dropShadow)
                    .shadowOffsetX(config.shadowOffsetX)
                    .shadowOffsetY(config.shadowOffsetY);
        }

        public FontConfig.Builder renderer(FontRendererType renderer) {
            this.renderer = renderer;
            return this;
        }

        public FontConfig.Builder face(String face) {
            this.face = face;
            return this;
        }

        public FontConfig.Builder size(double size) {
            this.size = size;
            return this;
        }

        public FontConfig.Builder scale(int scale) {
            this.scale = scale;
            return this;
        }

        public FontConfig.Builder antiAliasing(boolean antiAliasing) {
            this.antiAliasing = antiAliasing;
            return this;
        }

        public FontConfig.Builder letterSpacing(double letterSpacing) {
            this.letterSpacing = letterSpacing;
            return this;
        }

        public FontConfig.Builder lineSpacing(double lineSpacing) {
            this.lineSpacing = lineSpacing;
            return this;
        }

        public FontConfig.Builder dropShadow(boolean dropShadow) {
            this.dropShadow = dropShadow;
            return this;
        }

        public FontConfig.Builder shadowOffsetX(int shadowOffsetX) {
            this.shadowOffsetX = shadowOffsetX;
            return this;
        }

        public FontConfig.Builder shadowOffsetY(int shadowOffsetY) {
            this.shadowOffsetY = shadowOffsetY;
            return this;
        }

        public FontConfig build() {
            return new FontConfig(renderer, face, size, scale, antiAliasing, letterSpacing, lineSpacing, dropShadow, shadowOffsetX, shadowOffsetY);
        }
    }

    // </generated-code>
}