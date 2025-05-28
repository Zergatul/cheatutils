package com.zergatul.cheatutils.font;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import com.zergatul.cheatutils.utils.GlobalTicks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class FontLibrary {

    public static final FontLibrary instance = new FontLibrary();

    private final Logger logger = LogManager.getLogger(FontLibrary.class);
    private final Map<FontKey, FontEntry> fonts = new HashMap<>();
    private final Map<FontKey, CompletableFuture<FontEntry>> fontFutures = new HashMap<>();
    private final Map<FontParameters, GlyphRendererEntry> renderers = new HashMap<>();

    private FontLibrary() {
        Events.ClientTickStart.add(this::onTickStart);
    }

    public CompletableFuture<GlyphRenderer> createRenderer(FontParameters parameters) {
        assert RenderSystem.isOnRenderThread();

        GlyphRendererEntry rendererEntry = renderers.get(parameters);
        if (rendererEntry != null) {
            return CompletableFuture.completedFuture(rendererEntry.renderer);
        }

        FontKey key = new FontKey(parameters);
        FontEntry fontEntry = fonts.get(key);
        if (fontEntry != null) {
            GlyphRenderer renderer = fontEntry.createRenderer(parameters.asRenderParameters());
            logger.info("Created glyph renderer from existing font reference: {}", parameters);
            renderers.put(parameters, new GlyphRendererEntry(renderer, fontEntry));
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
            }, TickEndExecutor.instance);
            fontFutures.put(key, fontFuture);
        }

        CompletableFuture<GlyphRenderer> future = new CompletableFuture<>();
        fontFuture.thenAcceptAsync(entry -> {
            GlyphRenderer renderer = entry.createRenderer(parameters.asRenderParameters());
            logger.info("Created glyph renderer asynchronously: {}", parameters);
            renderers.put(parameters, new GlyphRendererEntry(renderer, entry));
            future.complete(renderer);
        }, TickEndExecutor.instance);

        return future;
    }

    private void onTickStart() {
        if (GlobalTicks.get() % 100 == 0) {
            if (removeStaleGlyphRenderers()) {
                removeUnusedFonts();
            }
        }
    }

    private boolean removeStaleGlyphRenderers() {
        boolean removed = false;
        Iterator<Map.Entry<FontParameters, GlyphRendererEntry>> iterator = renderers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<FontParameters, GlyphRendererEntry> entry = iterator.next();
            GlyphRenderer renderer = entry.getValue().renderer;
            if (renderer.isStale()) {
                if (GlyphRendererHolders.getHolders().noneMatch(h -> h.uses(renderer))) {
                    logger.info("Releasing glyph renderer: {}", entry.getKey());
                    iterator.remove();
                    entry.getValue().fontEntry.renderers.remove(renderer);
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

    private record FontEntry(FontReference font, List<GlyphRenderer> renderers) {
        public FontEntry(FontReference font) {
            this(font, new ArrayList<>());
        }

        public GlyphRenderer createRenderer(FontRenderParameters parameters) {
            GlyphRenderer renderer = font.createGlyphRenderer(parameters);
            renderers.add(renderer);
            return renderer;
        }
    }

    private record GlyphRendererEntry(GlyphRenderer renderer, FontEntry fontEntry) {}
}