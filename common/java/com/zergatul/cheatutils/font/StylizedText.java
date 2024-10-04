package com.zergatul.cheatutils.font;

import net.minecraft.network.chat.Style;

import java.util.ArrayList;

public class StylizedText {

    public final ArrayList<StylizedTextChunk> chunks = new ArrayList<>(4);

    public static StylizedText of(String value) {
        StylizedText text = new StylizedText();
        text.chunks.add(new StylizedTextChunk(value, Style.EMPTY));
        return text;
    }

    public void append(String value, Style style) {
        chunks.add(new StylizedTextChunk(value, style));
    }

    public int length() {
        int length = 0;
        for (StylizedTextChunk chunk : chunks) {
            length += chunk.text().length();
        }
        return length;
    }
}