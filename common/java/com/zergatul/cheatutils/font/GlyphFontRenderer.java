package com.zergatul.cheatutils.font;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

public class GlyphFontRenderer {

    private final Font font;
    private final boolean antiAliasing;
    private final Map<Character, Glyph> glyphs;

    public GlyphFontRenderer(Font font, boolean antiAliasing) {
        this.font = font;
        this.antiAliasing = antiAliasing;
        glyphs = new HashMap<>();
    }

    public TextBounds getTextSize(String string) {
        if (string == null) {
            return new TextBounds(0, 0, 0, 0);
        }

        ensureGlyphs(string);
        int width = 0;
        int height = 0;
        int top = Integer.MAX_VALUE;
        int bottom = Integer.MAX_VALUE;
        for (int i = 0; i < string.length(); i++) {
            Glyph glyph = glyphs.get(string.charAt(i));
            width += glyph.getWidth();
            if (glyph.getHeight() > height) {
                height = glyph.getHeight();
            }
            if (glyph.getTop() < top) {
                top = glyph.getTop();
            }
            if (glyph.getBottom() < bottom) {
                bottom = glyph.getBottom();
            }
        }

        return new TextBounds(width, height, top, bottom);
    }

    public TextBounds getTextSize(StylizedText text) {
        if (text == null) {
            return new TextBounds(0, 0, 0, 0);
        }

        for (StylizedTextChunk chunk : text.chunks) {
            ensureGlyphs(chunk.text());
        }

        int width = 0;
        int height = 0;
        int top = Integer.MAX_VALUE;
        int bottom = Integer.MAX_VALUE;
        for (StylizedTextChunk chunk : text.chunks) {
            String string = chunk.text();
            for (int i = 0; i < string.length(); i++) {
                Glyph glyph = glyphs.get(string.charAt(i));
                width += glyph.getWidth();
                if (glyph.getHeight() > height) {
                    height = glyph.getHeight();
                }
                if (glyph.getTop() < top) {
                    top = glyph.getTop();
                }
                if (glyph.getBottom() < bottom) {
                    bottom = glyph.getBottom();
                }
            }
        }

        return new TextBounds(width, height, top, bottom);
    }

    public void drawText(PoseStack stack, String string, float x, float y, double invScale) {
        if (string == null) {
            return;
        }

        setupRenderState();
        drawText(stack, string, x, y, invScale, 1, 1, 1, 1);
    }

    public void drawText(PoseStack stack, StylizedText text, float x, float y, double invScale) {
        if (text == null) {
            return;
        }

        setupRenderState();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        for (StylizedTextChunk chunk : text.chunks) {
            int color = chunk.color();
            float r = (float)(color >> 16 & 0xFF) / 255;
            float g = (float)(color >> 8 & 0xFF) / 255;
            float b = (float)(color & 0xFF) / 255;
            x = drawText(stack, chunk.text(), x, y, invScale, r, g, b, 1);
        }
    }

    private void setupRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        GlStateManager._texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    }

    private float drawText(PoseStack stack, String string, float x, float y, double invScale, float r, float g, float b, float a) {
        ensureGlyphs(string);
        for (int i = 0; i < string.length(); i++) {
            Glyph glyph = glyphs.get(string.charAt(i));
            glyph.bindTexture();
            float width = (float)(glyph.getWidth() * invScale);
            float height = (float)(glyph.getHeight() * invScale);

            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();

            RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

            bufferBuilder
                    .vertex(stack.last().pose(), x, y + height, 0)
                    .color(r, g, b, a)
                    .uv(0, 1).endVertex();
            bufferBuilder
                    .vertex(stack.last().pose(), x + width, y + height, 0)
                    .color(r, g, b, a)
                    .uv(1, 1).endVertex();
            bufferBuilder
                    .vertex(stack.last().pose(), x + width, y, 0)
                    .color(r, g, b, a)
                    .uv(1, 0).endVertex();
            bufferBuilder
                    .vertex(stack.last().pose(), x, y, 0)
                    .color(r, g, b, a)
                    .uv(0, 0)
                    .endVertex();

            BufferUploader.drawWithShader(bufferBuilder.end());

            x += width;
        }
        return x;
    }

    public void dispose() {
        for (Glyph glyph : glyphs.values()) {
            glyph.dispose();
        }
    }

    private void ensureGlyphs(String string) {
        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if (!glyphs.containsKey(ch)) {
                glyphs.put(ch, new Glyph(font, ch, antiAliasing));
            }
        }
    }
}