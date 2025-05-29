package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.utils.ColorUtils;
import net.minecraft.network.chat.Style;

import java.util.*;
import java.util.stream.IntStream;

public class StylizedText {

    public final ArrayList<StylizedTextChunk> chunks = new ArrayList<>(4);

    public static StylizedText empty() {
        return new StylizedText();
    }

    public static StylizedText of(String value) {
        return of(value, Style.EMPTY);
    }

    public static StylizedText of(String value, Style style) {
        StylizedText text = new StylizedText();
        text.chunks.add(new StylizedTextChunk(value, style));
        return text;
    }

    public static StylizedText createSafe(String text, String color) {
        return of(text, createStyleSafe(color));
    }

    public static StylizedText createSafe(String[] parameters) {
        if (parameters.length == 0 || parameters.length % 2 != 0) {
            return empty();
        } else {
            StylizedText text = empty();
            for (int i = 0; i < parameters.length; i += 2) {
                text.append(parameters[i + 1], createStyleSafe(parameters[i]));
            }
            return text;
        }
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

    private static Style createStyleSafe(String color) {
        Integer colorInt = ColorUtils.parseColor(color);
        return colorInt != null ? Style.EMPTY.withColor(colorInt) : Style.EMPTY;
    }
}