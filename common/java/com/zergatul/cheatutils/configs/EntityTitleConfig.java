package com.zergatul.cheatutils.configs;

public class EntityTitleConfig implements ValidatableConfig {

    public FontConfig titleFont;
    public FontConfig enchantmentFont;

    public EntityTitleConfig() {
        titleFont = new FontConfig();
        enchantmentFont = new FontConfig();
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
    }
}