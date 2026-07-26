package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class StbFont {

    private static final Logger LOGGER = LogManager.getLogger(StbFont.class);

    private final STBTTFontinfo info;
    private final int ascent;
    private final int descent;
    private final int lineGap;

    protected StbFont(SystemFontInfo systemInfo, STBTTFontinfo stbInfo, ByteBuffer buffer) {
        this.info = stbInfo;

        IntBuffer ascent  = BufferUtils.createIntBuffer(1);
        IntBuffer descent = BufferUtils.createIntBuffer(1);
        IntBuffer lineGap = BufferUtils.createIntBuffer(1);
        STBTruetype.stbtt_GetFontVMetrics(stbInfo, ascent, descent, lineGap);
        this.ascent = ascent.get(0);
        this.descent = descent.get(0);
        this.lineGap = lineGap.get(0);

        SharedCleaner.register(this, new StbFontCleaner(systemInfo.name, stbInfo, buffer));
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

    private record StbFontCleaner(String fontName, STBTTFontinfo stbInfo, ByteBuffer buffer) implements Runnable {
        @Override
        public void run() {
            LOGGER.info("Releasing STB font: {}", fontName);
            ClientTickEndExecutor.instance.execute(() -> {
                stbInfo.free();
                buffer.clear();
            });
        }
    }
}