package com.zergatul.cheatutils.configs;

public class StatusOverlayConfig extends ModuleConfig implements ValidatableConfig {

    public String code;
    public FontConfig font;

    public StatusOverlayConfig() {
        font = new FontConfig();
    }

    @Override
    public void validate() {
        if (font == null) {
            font = new FontConfig();
        }
        font.validate();
    }
}