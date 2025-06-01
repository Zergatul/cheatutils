package com.zergatul.cheatutils.font;

import java.awt.Font;

public class AwtFontReference extends FontReference {

    private final SystemFontInfo info;
    private final Font font;

    protected AwtFontReference(SystemFontInfo info, Font font) {
        this.info = info;
        this.font = font;
    }

    @Override
    public FontBackend createFontBackend(FontRenderParameters parameters) {
        return new AwtGlyphRenderer(info, font, parameters);
    }
}