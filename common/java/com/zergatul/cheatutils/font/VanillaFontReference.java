package com.zergatul.cheatutils.font;

public class VanillaFontReference extends FontReference {

    protected VanillaFontReference() {}

    @Override
    public FontBackend createFontBackend(FontRenderParameters parameters) {
        return new VanillaFontBackend();
    }
}