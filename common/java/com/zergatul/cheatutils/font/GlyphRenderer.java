package com.zergatul.cheatutils.font;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import com.zergatul.cheatutils.render.AtlasTexture;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class GlyphRenderer extends FontBackend {

    private static final Logger LOGGER = LogManager.getLogger(GlyphRenderer.class);

    protected final AtlasTexture texture;
    protected final Int2ObjectMap<Glyph> glyphs;

    protected GlyphRenderer(String rendererName) {
        assert RenderSystem.isOnRenderThread();

        this.texture = new AtlasTexture();
        this.glyphs = new Int2ObjectOpenHashMap<>();

        SharedCleaner.register(this, new GlyphRendererCleaner(rendererName, this.texture));
    }

    public void ensureGlyphs(String text) {
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (!glyphs.containsKey(ch)) {
                glyphs.put(ch, renderGlyph(ch));
            }
        }
    }

    public Glyph get(char ch) {
        return glyphs.get(ch);
    }

    public abstract float getLineHeight();

    protected abstract Glyph renderGlyph(char ch);

    private record GlyphRendererCleaner(String rendererName, AtlasTexture texture) implements Runnable {
        @Override
        public void run() {
            LOGGER.info("Releasing atlas texture: {}", rendererName);
            ClientTickEndExecutor.instance.execute(texture::dispose);
        }
    }
}