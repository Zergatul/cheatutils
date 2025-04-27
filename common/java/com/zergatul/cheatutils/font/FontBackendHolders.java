package com.zergatul.cheatutils.font;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FontBackendHolders {

    private static final List<FontBackendHolder> list = new ArrayList<>();

    public static void add(FontBackendHolder holder) {
        list.add(holder);
    }

    public static Stream<FontBackendHolder> getHolders() {
        return list.stream();
    }
}