package com.zergatul.cheatutils.font;

public class FailedFontReference extends FontReference {
    @Override
    public FontBackend createFontBackend(FontRenderParameters parameters) {
        return new FailedFontBackend(parameters.size());
    }
}