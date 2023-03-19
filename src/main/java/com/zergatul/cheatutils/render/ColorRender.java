package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.systems.RenderSystem;

import java.awt.*;

public class ColorRender {

    public static void setShaderColor(Color color) {
        RenderSystem.setShaderColor(
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                color.getAlpha() / 255f);
    }

    public static void setShaderColor(int color) {
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
        RenderSystem.setShaderColor(r, g, b, a);
    }
}