package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.utils.ColorUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.stream.IntStream;

public class StylizedText {

    public final ArrayList<StylizedTextChunk> chunks = new ArrayList<>(4);

    public static StylizedText empty() {
        return new StylizedText();
    }

    public static StylizedText of(String value) {
        return of(value, Color.WHITE.getRGB());
    }

    public static StylizedText of(String value, int color) {
        StylizedText text = new StylizedText();
        text.chunks.add(new StylizedTextChunk(value, color));
        return text;
    }

    public static StylizedText createSafe(String text, String color) {
        return of(text, parseColorSafe(color));
    }

    public static StylizedText createSafe(String[] parameters) {
        if (parameters.length == 0 || parameters.length % 2 != 0) {
            return empty();
        }

        StylizedText text = empty();
        for (int i = 0; i < parameters.length; i += 2) {
            text.append(parameters[i + 1], parseColorSafe(parameters[i]));
        }
        return text;
    }

    public void append(String value, int color) {
        chunks.add(new StylizedTextChunk(value, color));
    }

    public void append(StylizedText text) {
        chunks.addAll(text.chunks);
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

    private static int parseColorSafe(String color) {
        Integer colorInt = ColorUtils.parseColor(color);
        return colorInt != null ? colorInt : Color.WHITE.getRGB();
    }
}