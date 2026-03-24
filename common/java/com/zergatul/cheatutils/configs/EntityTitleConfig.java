package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class EntityTitleConfig implements Sanitizable {

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
    public void sanitize() {
        if (titleFont == null) {
            titleFont = new FontConfig();
        }
        titleFont.sanitize();

        if (enchantmentFont == null) {
            enchantmentFont = new FontConfig();
        }
        enchantmentFont.sanitize();

        itemScale = MathUtils.clamp(itemScale, 0, 50);
    }
}