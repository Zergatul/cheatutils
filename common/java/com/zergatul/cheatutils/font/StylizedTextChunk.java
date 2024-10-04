package com.zergatul.cheatutils.font;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public record StylizedTextChunk(String text, Style style) {

    public int getColor() {
        TextColor color = style.getColor();
        return color != null ? color.getValue() : 0xFFFFFFFF;
    }

    public void setShaderColor() {
        int color = getColor();
        float r = (float) (color >> 16 & 0xFF) / 255;
        float g = (float) (color >> 8 & 0xFF) / 255;
        float b = (float) (color & 0xFF) / 255;
        RenderSystem.setShaderColor(r, g, b, 1f);
    }
}