package com.zergatul.cheatutils.font;

public record FontParameters(FontRendererType type, String name, float size, boolean antiAliasing) {
    public FontRenderParameters asRenderParameters() {
        return new FontRenderParameters(size, antiAliasing);
    }
}