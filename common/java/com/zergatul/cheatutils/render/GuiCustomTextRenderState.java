package com.zergatul.cheatutils.render;

import com.zergatul.cheatutils.font.FontRenderer;
import com.zergatul.cheatutils.font.StylizedText;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.ScreenArea;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class GuiCustomTextRenderState implements ScreenArea {

    public final FontRenderer font;
    public final StylizedText text;
    public final Matrix3x2fc pose;
    public final int x;
    public final int y;
    public final @Nullable ScreenRectangle scissor;
    private @Nullable ScreenRectangle bounds;
    private @Nullable List<CustomGlyph> glyphs;

    public GuiCustomTextRenderState(
            FontRenderer font,
            StylizedText text,
            Matrix3x2fc pose,
            int x,
            int y
    ) {
        this.font = font;
        this.text = text;
        this.pose = pose;
        this.x = x;
        this.y = y;
        this.scissor = null;
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        return this.bounds;
    }

    public CustomTextRenderState asPrepared() {
        if (this.glyphs == null) {
            // TODO
        }

        return new CustomTextRenderState();
    }
}