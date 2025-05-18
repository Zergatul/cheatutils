package com.zergatul.cheatutils.font;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.common.Events;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FontLibrary {

    public static final FontLibrary instance = new FontLibrary();

    private final Map<String, FontOld> loadedFonts = new HashMap<>();
    private final Map<String, CompletableFuture<FontOld>> loadingFonts = new HashMap<>();

    private FontLibrary() {
        Events.ClientTickStart.add(this::tick);
    }

    public FontOld get(String name) {
        assert RenderSystem.isOnRenderThread();

        FontOld font = loadedFonts.get(name);
        if (font != null) {
            return font;
        }

        if (!loadingFonts.containsKey(name)) {
            Optional<SystemFontInfo> optional = SystemFonts.getFontsBlocking().stream().filter(f -> f.getName().equals(name)).findFirst();
            if (optional.isPresent()) {
                loadingFonts.put(name, CompletableFuture.supplyAsync(() -> optional.get().load()));
            } else {
                loadedFonts.put(name, FailedFontOld.instance);
            }
        }

        return null;
    }

    private void tick() {
        assert RenderSystem.isOnRenderThread();

        if (loadingFonts.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<String, CompletableFuture<FontOld>>> iterator = loadingFonts.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, CompletableFuture<FontOld>> entry = iterator.next();
            CompletableFuture<FontOld> future = entry.getValue();
            if (future.isDone()) {
                iterator.remove();
                if (future.isCompletedExceptionally()) {
                    loadedFonts.put(entry.getKey(), FailedFontOld.instance);
                } else {
                    loadedFonts.put(entry.getKey(), future.join());
                }
            }
        }
    }
}