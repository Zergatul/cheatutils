package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.render.Position2dColorRenderer;
import com.zergatul.cheatutils.render.buffers.RenderBuffers;

public class FailedFontRenderer extends FontRenderer {

    private final FontBackend backend;
    private final int size;

    public FailedFontRenderer(FontBackend backend, int size) {
        this.backend = backend;
        this.size = size;
    }

    @Override
    public boolean uses(FontBackend backend) {
        return this.backend == backend;
    }

    @Override
    public TextBounds getTextSize(StylizedText text) {
        backend.markUse();
        return new TextBounds(text.length() * size, 0, size);
    }

    @Override
    public float getLineHeight() {
        return size;
    }

    @Override
    public void drawText(RenderBuffers buffers, StylizedText text, float x, float y) {
        backend.markUse();

        if (text == null) {
            return;
        }

        Position2dColorRenderer.BufferBuilder buffer = buffers.getColor2d();
        for (StylizedTextChunk chunk : text.chunks) {
            for (int i = 0; i < chunk.text().length(); i++) {
                buffer.rect(x + 1, y + 1, size - 2, size - 2, chunk.color());
                x += size;
            }
        }
    }
}