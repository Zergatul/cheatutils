package com.zergatul.cheatutils.font;

import net.minecraft.network.chat.Style;

import java.util.*;
import java.util.stream.IntStream;

public class StylizedText {

    public final ArrayList<StylizedTextChunk> chunks = new ArrayList<>(4);

    public static StylizedText of(String value) {
        return of(value, Style.EMPTY);
    }

    public static StylizedText of(String value, Style style) {
        StylizedText text = new StylizedText();
        text.chunks.add(new StylizedTextChunk(value, style));
        return text;
    }

    public void append(String value, Style style) {
        chunks.add(new StylizedTextChunk(value, style));
    }

    public IntStream chars() {
        return chunks.stream().map(StylizedTextChunk::text).flatMapToInt(String::chars);
    }

    public int length() {
        int length = 0;
        for (StylizedTextChunk chunk : chunks) {
            length += chunk.text().length();
        }
        return length;
    }
}