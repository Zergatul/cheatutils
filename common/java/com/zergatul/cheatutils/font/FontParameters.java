package com.zergatul.cheatutils.font;

import java.util.Objects;

public class FontParameters {

    private final FontRendererType type;
    private final String name;
    private final float size;
    private final boolean antiAliasing;

    public FontParameters(FontRendererType type, String name, float size, boolean antiAliasing) {
        this.type = type;
        this.name = name;
        this.size = size;
        if (type == FontRendererType.STB) {
            this.antiAliasing = true;
        } else {
            this.antiAliasing = antiAliasing;
        }
    }

    public FontRendererType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public FontRenderParameters asRenderParameters() {
        return new FontRenderParameters(size, antiAliasing);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FontParameters other) {
            return other.type == type && other.name.equals(name) && other.size == size && other.antiAliasing == antiAliasing;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, size, antiAliasing);
    }

    @Override
    public String toString() {
        return String.format("[type=%s, name=%s, size=%s, aa=%s]", type, name, size, antiAliasing);
    }
}