package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4fc;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class VanillaFontHelper {

    public static void drawInBatch(
            Font font,
            String text,
            float x,
            float y,
            int color,
            boolean drawShadow,
            Matrix4fc pose,
            MultiBufferSource bufferSource,
            Font.DisplayMode displayMode,
            int backgroundColor,
            int packedLightCoords
    ) {
        Font.PreparedText prepared = font.prepareText(text, x, y, color, drawShadow, backgroundColor);
        prepared.visit(createGlyphVisitor(bufferSource, pose, displayMode, packedLightCoords));
    }

    public static void drawInBatch(
            Font font,
            Component text,
            float x,
            float y,
            int color,
            boolean drawShadow,
            Matrix4fc pose,
            MultiBufferSource bufferSource,
            Font.DisplayMode displayMode,
            int backgroundColor,
            int packedLightCoords
    ) {
        Font.PreparedText prepared = font.prepareText(
                text.getVisualOrderText(),
                x, y,
                color,
                drawShadow,
                false,
                backgroundColor);
        prepared.visit(createGlyphVisitor(bufferSource, pose, displayMode, packedLightCoords));
    }

    public static void drawInBatch(
            Font font,
            FormattedCharSequence text,
            float x,
            float y,
            int color,
            boolean drawShadow,
            Matrix4fc pose,
            MultiBufferSource bufferSource,
            Font.DisplayMode displayMode,
            int backgroundColor,
            int packedLightCoords
    ) {
        Font.PreparedText prepared = font.prepareText(
                text,
                x, y,
                color,
                drawShadow,
                false,
                backgroundColor);
        prepared.visit(createGlyphVisitor(bufferSource, pose, displayMode, packedLightCoords));
    }

    private static Font.GlyphVisitor createGlyphVisitor(
            MultiBufferSource bufferSource,
            Matrix4fc pose,
            Font.DisplayMode displayMode,
            int packedLightCoords
    ) {
        return new Font.GlyphVisitor() {
            @Override
            public void acceptRenderable(TextRenderable renderable) {
                VertexConsumer buffer = bufferSource.getBuffer(renderable.renderType(displayMode));
                renderable.render(pose, buffer, packedLightCoords, false);
            }
        };
    }
}
