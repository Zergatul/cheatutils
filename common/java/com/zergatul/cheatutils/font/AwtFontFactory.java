package com.zergatul.cheatutils.font;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Font;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AwtFontFactory extends FontFactory {

    private final Logger logger = LogManager.getLogger(AwtFontFactory.class);

    protected AwtFontFactory() {}

    @Override
    public CompletableFuture<FontReference> create(String name) {
        Optional<SystemFontInfo> optional = SystemFonts.getFontsBlocking()
                .stream()
                .filter(f -> f.getName().equals(name))
                .findFirst();
        if (optional.isEmpty()) {
            return CompletableFuture.completedFuture(new FailedFontReference());
        }

        SystemFontInfo info = optional.get();
        return CompletableFuture.supplyAsync(() -> {
            Font font;
            try {
                font = info.createAwtFont();
            } catch (Throwable e) {
                logger.error(e);
                return new FailedFontReference();
            }
            return new AwtFontReference(info, font);
        });
    }
}