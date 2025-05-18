package com.zergatul.cheatutils.font;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class GlyphRendererHolders {

    private static final List<GlyphRendererHolder> list = new ArrayList<>();

    public static void add(GlyphRendererHolder holder) {
        list.add(holder);
    }

    public static Stream<GlyphRendererHolder> getHolders() {
        return list.stream();
    }
}