package com.zergatul.cheatutils.render;

import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import org.joml.Matrix4f;

public class Primitives {

    public static void fill(Matrix4f matrix, float x, float y, float width, float height, int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        Color2dRenderer renderer = RenderUtilities.instance.getColor2dRenderer();
        renderer.begin();
        renderer.rect(x, y, width, height, r, g, b, a);
        renderer.end(matrix);
    }
}