package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class EntityTitleConfig implements ValidatableConfig {

    public int fontSize;
    public boolean antiAliasing;
    public int enchFontSize;
    public boolean enchAntiAliasing;

    public EntityTitleConfig() {
        fontSize = 12;
        enchFontSize = 12;
    }

    @Override
    public void validate() {
        fontSize = MathUtils.clamp(fontSize, 8, 100);
        enchFontSize = MathUtils.clamp(enchFontSize, 8, 100);
    }
}