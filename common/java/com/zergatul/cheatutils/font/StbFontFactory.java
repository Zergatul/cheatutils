package com.zergatul.cheatutils.font;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class StbFontFactory extends FontFactory {

    protected StbFontFactory() {}

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
            StbFont font;
            try {
                font = info.createStbFont();
            } catch (Throwable e) {
                throw new RuntimeException(); // TODO - return failed font
            }
            return new StbFontReference(font);
        });
    }
}