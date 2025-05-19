package com.zergatul.cheatutils.font;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import com.zergatul.cheatutils.render.gl.AtlasTexture;
import com.zergatul.cheatutils.utils.GlobalTicks;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public abstract class GlyphRenderer {

    protected final AtlasTexture texture;
    protected final Int2ObjectMap<Glyph> glyphs;
    protected long lastUsed;

    protected GlyphRenderer() {
        assert RenderSystem.isOnRenderThread();

        this.texture = new AtlasTexture();
        this.glyphs = new Int2ObjectOpenHashMap<>();
        this.lastUsed = GlobalTicks.get();

        SharedCleaner.register(this, new GlyphRendererCleaner(this.texture));
    }

    public abstract FontRenderer createFontRenderer(FontRenderDetails details);

    public void ensureGlyphs(String text) {
        lastUsed = GlobalTicks.get();

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

    public boolean isStale() {
        return (GlobalTicks.get() - lastUsed) > 10 * 60 * 20; // 10 minutes
    }

    protected abstract Glyph renderGlyph(char ch);

    private record GlyphRendererCleaner(AtlasTexture texture) implements Runnable {
        @Override
        public void run() {
            TickEndExecutor.instance.execute(texture::dispose);
        }
    }
}