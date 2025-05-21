package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.font.FontParameters;
import com.zergatul.cheatutils.font.FontRenderDetails;
import com.zergatul.cheatutils.font.FontRendererType;
import com.zergatul.cheatutils.utils.MathUtils;

public class FontConfig implements ValidatableConfig {

    public String face;
    public FontRendererType renderer;
    public double size;
    public boolean antiAliasing;
    public double letterSpacing;
    public double lineSpacing;

    public FontConfig() {
        this.face = "Consolas";
        this.renderer = FontRendererType.AWT;
        this.size = 16;
        this.antiAliasing = false;
    }

    public boolean equals(FontConfig other) {
        return  other.face.equals(face) &&
                other.renderer == renderer &&
                other.size == size &&
                other.antiAliasing == antiAliasing &&
                other.letterSpacing == letterSpacing &&
                other.lineSpacing == lineSpacing;
    }

    public FontParameters asFontParameters() {
        return new FontParameters(renderer, face, (float) size, antiAliasing);
    }

    public FontRenderDetails asFontRenderDetails() {
        return new FontRenderDetails((float) letterSpacing, (float) lineSpacing);
    }

    @Override
    public void validate() {
        size = MathUtils.clamp(size, 8, 100);
        letterSpacing = MathUtils.clamp(letterSpacing, -10, 10);
        lineSpacing = MathUtils.clamp(lineSpacing, -50, 50);
    }
}