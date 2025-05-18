package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.font.FontParameters;
import com.zergatul.cheatutils.font.FontRenderDetails;
import com.zergatul.cheatutils.font.FontRendererType;
import com.zergatul.cheatutils.utils.MathUtils;

public class FontConfig implements ValidatableConfig {

    public String face;
    public int size;
    public boolean antiAliasing;
    public double letterSpacing;

    public FontConfig() {
        this.face = "Consolas";
        this.size = 16;
        this.antiAliasing = false;
    }

    public boolean equals(FontConfig other) {
        return  other.face.equals(face) &&
                other.size == size &&
                other.antiAliasing == antiAliasing &&
                other.letterSpacing == letterSpacing;
    }

    public FontParameters asFontParameters() {
        return new FontParameters(FontRendererType.AWT, face, size, antiAliasing);
    }

    public FontRenderDetails asFontRenderDetails() {
        return new FontRenderDetails((float) letterSpacing);
    }

    @Override
    public void validate() {
        size = MathUtils.clamp(size, 8, 100);
        letterSpacing = MathUtils.clamp(letterSpacing, -10, 10);
    }
}