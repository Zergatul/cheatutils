package com.zergatul.cheatutils.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.NullMarked;

import java.util.function.Consumer;

@NullMarked
public class VanillaFontHelper {

    public static void visit(
            Font font,
            String text,
            float x,
            float y,
            int color,
            boolean drawShadow,
            int backgroundColor,
            Consumer<TextRenderable> consumer
    ) {
        Font.PreparedText prepared = font.prepareText(text, x, y, color, drawShadow, backgroundColor);
        prepared.visit(createGlyphVisitor(consumer));
    }

    public static void visit(
            Font font,
            Component text,
            float x,
            float y,
            int color,
            boolean drawShadow,
            int backgroundColor,
            Consumer<TextRenderable> consumer
    ) {
        Font.PreparedText prepared = font.prepareText(
                text.getVisualOrderText(),
                x, y,
                color,
                drawShadow,
                false,
                backgroundColor);
        prepared.visit(createGlyphVisitor(consumer));
    }

    public static void visit(
            Font font,
            FormattedCharSequence text,
            float x,
            float y,
            int color,
            boolean drawShadow,
            int backgroundColor,
            Consumer<TextRenderable> consumer
    ) {
        Font.PreparedText prepared = font.prepareText(
                text,
                x, y,
                color,
                drawShadow,
                false,
                backgroundColor);
        prepared.visit(createGlyphVisitor(consumer));
    }

    private static Font.GlyphVisitor createGlyphVisitor(Consumer<TextRenderable> consumer) {
        return new Font.GlyphVisitor() {
            @Override
            public void acceptRenderable(TextRenderable renderable) {
                consumer.accept(renderable);
            }
        };
    }
}