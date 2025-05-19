package com.zergatul.cheatutils.font;

import java.awt.Font;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AwtFontFactory extends FontFactory {

    protected AwtFontFactory() {}

    @Override
    public CompletableFuture<FontReference> create(String name) {
        Optional<SystemFontInfo> optional = SystemFonts.getFontsBlocking()
                .stream()
                .filter(f -> f.getName().equals(name))
                .findFirst();
        if (optional.isEmpty()) {
            throw new RuntimeException(); // TODO - return failed font
        }

        SystemFontInfo info = optional.get();
        return CompletableFuture.supplyAsync(() -> {
            Font font;
            try {
                font = info.createAwtFont();
            } catch (Throwable e) {
                throw new RuntimeException(); // TODO - return failed font
            }
            return new AwtFontReference(font);
        });
    }
}