package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class EntityTitleConfig implements ValidatableConfig {

    public String hpPrefix;
    public FontConfig titleFont;
    public FontConfig enchantmentFont;
    public int itemScale;

    public EntityTitleConfig() {
        hpPrefix = "\u2665";
        titleFont = new FontConfig();
        enchantmentFont = new FontConfig();
        itemScale = 0;
    }

    @Override
    public void validate() {
        if (titleFont == null) {
            titleFont = new FontConfig();
        }
        titleFont.validate();

        if (enchantmentFont == null) {
            enchantmentFont = new FontConfig();
        }
        enchantmentFont.validate();

        itemScale = MathUtils.clamp(itemScale, 0, 50);
    }
}