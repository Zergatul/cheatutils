package com.zergatul.cheatutils.font;

public enum FontRendererType {
    AWT(new AwtFontFactory()),
    STB(new StbFontFactory());

    private final FontFactory factory;

    FontRendererType(FontFactory factory) {
        this.factory = factory;
    }

    public FontFactory getFactory() {
        return factory;
    }
}