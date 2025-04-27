package com.zergatul.cheatutils.font;

public record FontRenderParameters(float size, boolean antiAliasing) {

    public String toString(FontRendererType type) {
        if (type == FontRendererType.AWT) {
            return String.format("size=%s, aa=%s", size, antiAliasing);
        } else {
            return String.format("size=%s", size);
        }
    }
}