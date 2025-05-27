package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.font.FontParameters;
import com.zergatul.cheatutils.font.FontRenderDetails;
import com.zergatul.cheatutils.font.FontRendererType;
import com.zergatul.cheatutils.utils.MathUtils;

public class FontConfig implements ValidatableConfig {

    public FontRendererType renderer;
    public String face;
    public double size;
    public int scale;
    public boolean antiAliasing;
    public double letterSpacing;
    public double lineSpacing;

    public FontConfig() {
        this.renderer = FontRendererType.VANILLA;
        this.face = "Consolas";
        this.size = 16;
        this.scale = 0;
        this.antiAliasing = false;
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
                    other.lineSpacing == lineSpacing;
            case STB ->
                    other.face.equals(face) &&
                    other.size == size &&
                    other.letterSpacing == letterSpacing &&
                    other.lineSpacing == lineSpacing;
            case VANILLA ->
                    other.scale == scale;
        };
    }

    public FontParameters asFontParameters() {
        return new FontParameters(renderer, face, (float) size, antiAliasing);
    }

    public FontRenderDetails asFontRenderDetails() {
        return new FontRenderDetails((float) letterSpacing, (float) lineSpacing, scale);
    }

    @Override
    public void validate() {
        size = MathUtils.clamp(size, 8, 100);
        letterSpacing = MathUtils.clamp(letterSpacing, -10, 10);
        lineSpacing = MathUtils.clamp(lineSpacing, -50, 50);
    }
}