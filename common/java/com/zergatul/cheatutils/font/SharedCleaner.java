package com.zergatul.cheatutils.font;

import java.lang.ref.Cleaner;

public class SharedCleaner {

    private static final Cleaner CLEANER = Cleaner.create();

    public static void register(Object obj, Runnable action) {
        CLEANER.register(obj, action);
    }
}