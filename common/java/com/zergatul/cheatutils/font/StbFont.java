package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class StbFont {

    private final STBTTFontinfo info;
    private final int ascent;
    private final int descent;
    private final int lineGap;

    protected StbFont(STBTTFontinfo info, ByteBuffer buffer) {
        this.info = info;

        IntBuffer ascent  = BufferUtils.createIntBuffer(1);
        IntBuffer descent = BufferUtils.createIntBuffer(1);
        IntBuffer lineGap = BufferUtils.createIntBuffer(1);
        STBTruetype.stbtt_GetFontVMetrics(info, ascent, descent, lineGap);
        this.ascent = ascent.get(0);
        this.descent = descent.get(0);
        this.lineGap = lineGap.get(0);

        SharedCleaner.register(this, new StbFontCleaner(info, buffer));
    }

    public STBTTFontinfo getFontInfo() {
        return info;
    }

    public float getLineHeight(float size) {
        return getScaleForPixelHeight(size) * (ascent - descent + lineGap);
    }

    protected float getScaleForPixelHeight(float pixels) {
        return pixels / (ascent - descent);
    }

    private record StbFontCleaner(STBTTFontinfo info, ByteBuffer buffer) implements Runnable {
        @Override
        public void run() {
            TickEndExecutor.instance.execute(() -> {
                info.free();
                buffer.clear();
            });
        }
    }
}