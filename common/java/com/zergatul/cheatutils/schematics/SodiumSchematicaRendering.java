package com.zergatul.cheatutils.schematics;

public final class SodiumSchematicaRendering {

    private static final ThreadLocal<Boolean> SHADED = new ThreadLocal<>();

    private SodiumSchematicaRendering() {}

    public static void begin(boolean shaded) {
        SHADED.set(shaded);
    }

    public static boolean isShaded() {
        return Boolean.TRUE.equals(SHADED.get());
    }

    public static void end() {
        SHADED.remove();
    }
}