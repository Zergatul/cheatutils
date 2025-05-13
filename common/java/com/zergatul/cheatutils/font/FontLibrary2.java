package com.zergatul.cheatutils.font;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.concurrent.TickEndExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FontLibrary2 {

    public static final FontLibrary2 instance = new FontLibrary2();

    private final Map<FontKey, FontReference> fonts = new HashMap<>();
    private final Map<FontKey, CompletableFuture<FontReference>> fontFutures = new HashMap<>();
    private final Map<FontParameters, FontRenderer> renderers = new HashMap<>();

    private FontLibrary2() {
        Events.ClientTickStart.add(this::onTickStart);
    }

    public CompletableFuture<FontRenderer> createRenderer(FontParameters parameters) {
        assert RenderSystem.isOnRenderThread();

        FontRenderer renderer = renderers.get(parameters);
        if (renderer != null) {
            return CompletableFuture.completedFuture(renderer);
        }

        FontKey key = new FontKey(parameters);
        FontReference font = fonts.get(key);
        if (font != null) {
            renderer = font.createFontRenderer(parameters.asRenderParameters());
            renderers.put(parameters, renderer);
            return CompletableFuture.completedFuture(renderer);
        }

        CompletableFuture<FontReference> fontFuture = fontFutures.get(key);
        if (fontFuture == null) {
            fontFuture = parameters.type().getFactory().create(parameters.name());
            fontFutures.put(key, fontFuture);

            fontFuture.thenAcceptAsync(font1 -> {
                fonts.put(key, font1);
                fontFutures.remove(key);
            }, TickEndExecutor.instance);
        }

        CompletableFuture<FontRenderer> future = new CompletableFuture<>();
        fontFuture.thenAcceptAsync(font1 -> {
            FontRenderer renderer1 = font1.createFontRenderer(parameters.asRenderParameters());
            renderers.put(parameters, renderer1);
            future.complete(renderer1);
        }, TickEndExecutor.instance);

        return future;
    }

    private void onTickStart() {

    }

    private record FontKey(FontRendererType type, String name) {
        public FontKey(FontParameters parameters) {
            this(parameters.type(), parameters.name());
        }
    }
}