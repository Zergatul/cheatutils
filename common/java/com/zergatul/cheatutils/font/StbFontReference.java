package com.zergatul.cheatutils.font;

public class StbFontReference extends FontReference {

    private final SystemFontInfo info;
    private final StbFont font;

    protected StbFontReference(SystemFontInfo info, StbFont font) {
        this.info = info;
        this.font = font;
    }

    @Override
    public FontBackend createFontBackend(FontRenderParameters parameters) {
        return new StbGlyphRenderer(info, font, parameters);
    }
}