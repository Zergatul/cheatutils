package com.zergatul.cheatutils.font;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import com.zergatul.cheatutils.utils.ClientTicks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class FontLibrary {

    public static final FontLibrary instance = new FontLibrary();

    private final Logger logger = LogManager.getLogger(FontLibrary.class);
    private final Map<FontKey, FontEntry> fonts = new HashMap<>();
    private final Map<FontKey, CompletableFuture<FontEntry>> fontFutures = new HashMap<>();
    private final Map<FontParameters, FontBackendEntry> backends = new HashMap<>();

    private FontLibrary() {
        Events.InGameTickStart.add(this::onTickStart);
    }

    public CompletableFuture<FontBackend> createBackend(FontParameters parameters) {
        assert RenderSystem.isOnRenderThread();

        FontBackendEntry backendEntry = backends.get(parameters);
        if (backendEntry != null) {
            return CompletableFuture.completedFuture(backendEntry.backend);
        }

        FontKey key = new FontKey(parameters);
        FontEntry fontEntry = fonts.get(key);
        if (fontEntry != null) {
            FontBackend renderer = fontEntry.createBackend(parameters.asRenderParameters());
            logger.info("Created font backend from existing font reference: {}", parameters);
            backends.put(parameters, new FontBackendEntry(renderer, fontEntry));
            return CompletableFuture.completedFuture(renderer);
        }

        CompletableFuture<FontEntry> fontFuture = fontFutures.get(key);
        if (fontFuture == null) {
            logger.info("Started font loading: {}", parameters);
            fontFuture = parameters.getType().getFactory().create(parameters.getName()).thenApplyAsync(font -> {
                logger.info("Finished font loading: {}", parameters);
                FontEntry entry = new FontEntry(font);
                fonts.put(key, entry);
                fontFutures.remove(key);
                return entry;
            }, ClientTickEndExecutor.instance);
            fontFutures.put(key, fontFuture);
        }

        CompletableFuture<FontBackend> future = new CompletableFuture<>();
        fontFuture.thenAcceptAsync(entry -> {
            FontBackend renderer = entry.createBackend(parameters.asRenderParameters());
            logger.info("Created font backend asynchronously: {}", parameters);
            backends.put(parameters, new FontBackendEntry(renderer, entry));
            future.complete(renderer);
        }, ClientTickEndExecutor.instance);

        return future;
    }

    private void onTickStart() {
        if (ClientTicks.get() % 100 == 0) {
            if (removeStaleFontBackends()) {
                removeUnusedFonts();
            }
        }
    }

    private boolean removeStaleFontBackends() {
        boolean removed = false;
        Iterator<Map.Entry<FontParameters, FontBackendEntry>> iterator = backends.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<FontParameters, FontBackendEntry> entry = iterator.next();
            FontBackend backend = entry.getValue().backend;
            if (backend.isStale()) {
                if (FontBackendHolders.getHolders().noneMatch(h -> h.uses(backend))) {
                    logger.info("Releasing font backend: {}", entry.getKey());
                    iterator.remove();
                    entry.getValue().fontEntry.renderers.remove(backend);
                    removed = true;
                }
            }
        }
        return removed;
    }

    private void removeUnusedFonts() {
        Iterator<Map.Entry<FontKey, FontEntry>> iterator = fonts.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<FontKey, FontEntry> entry = iterator.next();
            if (entry.getValue().renderers.isEmpty()) {
                logger.info("Releasing font reference: {}", entry.getKey());
                iterator.remove();
            }
        }
    }

    private record FontKey(FontRendererType type, String name) {
        public FontKey(FontParameters parameters) {
            this(parameters.getType(), parameters.getName());
        }
    }

    private record FontEntry(FontReference font, List<FontBackend> renderers) {
        public FontEntry(FontReference font) {
            this(font, new ArrayList<>());
        }

        public FontBackend createBackend(FontRenderParameters parameters) {
            FontBackend renderer = font.createFontBackend(parameters);
            renderers.add(renderer);
            return renderer;
        }
    }

    private record FontBackendEntry(FontBackend backend, FontEntry fontEntry) {}
}