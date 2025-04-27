package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class FontConfig implements ValidatableConfig {

    public String face;
    public int size;

    public FontConfig() {
        this("Consolas", 16);
    }

    public FontConfig(String face, int size) {
        this.face = face;
        this.size = size;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FontConfig other) {
            return other.face.equals(face) && other.size == size;
        } else {
            return false;
        }
    }

    @Override
    public void validate() {
        size = MathUtils.clamp(size, 8, 100);
    }
}