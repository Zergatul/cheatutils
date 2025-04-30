package com.zergatul.cheatutils.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;

public class RenderingContext {

    private final GuiGraphics graphics;
    private final Matrix4f matrix;
    private final int halfWidth;
    private final int halfHeight;
    private final int scale;
    private final Font font;

    public RenderingContext(GuiGraphics graphics, Matrix4f matrix, int halfWidth, int halfHeight) {
        this.graphics = graphics;
        this.matrix = matrix;
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;

        Minecraft mc = Minecraft.getInstance();
        this.scale = (int) mc.getWindow().getGuiScale(); // currently it is always integer
        this.font = mc.font;
    }

    public GuiGraphics getGraphics() {
        return graphics;
    }

    public Matrix4f getMatrix() {
        return matrix;
    }

    public int getScale() {
        return scale;
    }

    public Font getFont() {
        return font;
    }

    public int getHalfWidth() {
        return halfWidth;
    }

    public int getHalfHeight() {
        return halfHeight;
    }

    public void render(Element element, int x, int y, HorizontalAlign hAlign, VerticalAlign vAlign) {
        element.measure(this);

        int width = element.getMeasuredWidth();
        int height = element.getMeasuredHeight();

        if (hAlign == HorizontalAlign.CENTER) {
            x -= width / 2;
        } else {
            throw new RuntimeException();
        }
        if (vAlign == VerticalAlign.BOTTOM) {
            y -= height;
        } else {
            throw new RuntimeException();
        }

        element.layout(x, y, width, height);
        element.render(this);
    }
}