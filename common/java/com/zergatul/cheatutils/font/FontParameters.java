package com.zergatul.cheatutils.font;

import java.util.Objects;

public class FontParameters {

    private final FontRendererType type;
    private final String name;
    private final float size;
    private final boolean antiAliasing;

    public FontParameters(FontRendererType type, String name, float size, boolean antiAliasing) {
        this.type = type;
        if (type != FontRendererType.VANILLA) {
            this.name = name;
            this.size = size;
        } else {
            this.name = null;
            this.size = 0;
        }
        if (type == FontRendererType.STB) {
            this.antiAliasing = false;
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
            return other.type == type && Objects.equals(other.name, name) && other.size == size && other.antiAliasing == antiAliasing;
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
        return switch (type) {
            case AWT -> String.format("[type=%s, name=%s, size=%s, aa=%s]", type, name, size, antiAliasing);
            case STB -> String.format("[type=%s, name=%s, size=%s]", type, name, size);
            case VANILLA -> String.format("[type=%s]", type);
        };
    }
}