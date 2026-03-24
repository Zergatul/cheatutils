package com.zergatul.cheatutils.configs;

public class StatusOverlayConfig extends ModuleConfig implements Sanitizable {

    public String code;
    public FontConfig font;

    public StatusOverlayConfig() {
        font = new FontConfig();
    }

    @Override
    public void sanitize() {
        if (font == null) {
            font = new FontConfig();
        }
        font.sanitize();
    }
}