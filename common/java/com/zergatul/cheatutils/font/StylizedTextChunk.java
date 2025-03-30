package com.zergatul.cheatutils.font;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public record StylizedTextChunk(String text, Style style) {

    public int getColor() {
        TextColor color = style.getColor();
        return color != null ? color.getValue() : 0xFFFFFFFF;
    }
}