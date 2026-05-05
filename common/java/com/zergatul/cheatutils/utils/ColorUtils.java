package com.zergatul.cheatutils.utils;

import java.awt.*;
import java.util.Locale;

public class ColorUtils {

    public static Integer parseColor(String str) {
        if (str == null) {
            return null;
        }
        str = str.toLowerCase(Locale.ROOT);
        if (str.length() == 7) {
            if (str.charAt(0) != '#') {
                return null;
            }
            for (int i = 1; i < 7; i++) {
                char ch = str.charAt(i);
                if ('0' <= ch && ch <= '9') {
                    continue;
                }
                if ('a' <= ch && ch <= 'f') {
                    continue;
                }
                return null;
            }
            return  0xFF000000 |
                    Integer.parseInt(str.substring(1, 3), 16) << 16 |
                    Integer.parseInt(str.substring(3, 5), 16) << 8 |
                    Integer.parseInt(str.substring(5, 7), 16);
        }
        if (str.length() == 9) {
            if (str.charAt(0) != '#') {
                return null;
            }
            for (int i = 1; i < 9; i++) {
                char ch = str.charAt(i);
                if ('0' <= ch && ch <= '9') {
                    continue;
                }
                if ('a' <= ch && ch <= 'f') {
                    continue;
                }
                return null;
            }
            return  Integer.parseInt(str.substring(7, 9), 16) << 24 |
                    Integer.parseInt(str.substring(1, 3), 16) << 16 |
                    Integer.parseInt(str.substring(3, 5), 16) << 8 |
                    Integer.parseInt(str.substring(5, 7), 16);
        }
        return null;
    }

    public static Color parseColor2(String str) {
        if (str == null) {
            return null;
        }
        str = str.toLowerCase(Locale.ROOT);
        if (str.length() == 7) {
            if (str.charAt(0) != '#') {
                return null;
            }
            for (int i = 1; i < 7; i++) {
                char ch = str.charAt(i);
                if ('0' <= ch && ch <= '9') {
                    continue;
                }
                if ('a' <= ch && ch <= 'f') {
                    continue;
                }
                return null;
            }
            return new Color(
                    Integer.parseInt(str.substring(1, 3), 16),
                    Integer.parseInt(str.substring(3, 5), 16),
                    Integer.parseInt(str.substring(5, 7), 16));
        }
        if (str.length() == 9) {
            if (str.charAt(0) != '#') {
                return null;
            }
            for (int i = 1; i < 9; i++) {
                char ch = str.charAt(i);
                if ('0' <= ch && ch <= '9') {
                    continue;
                }
                if ('a' <= ch && ch <= 'f') {
                    continue;
                }
                return null;
            }
            return new Color(
                    Integer.parseInt(str.substring(1, 3), 16),
                    Integer.parseInt(str.substring(3, 5), 16),
                    Integer.parseInt(str.substring(5, 7), 16),
                    Integer.parseInt(str.substring(7, 9), 16));
        }
        return null;
    }

    public static float r(int color) {
        return ((color >>> 16) & 0xFF) / 255F;
    }

    public static float g(int color) {
        return ((color >>> 8) & 0xFF) / 255F;
    }

    public static float b(int color) {
        return (color & 0xFF) / 255F;
    }

    public static float a(int color) {
        return ((color >>> 24) & 0xFF) / 255F;
    }

    public static String asHexRGB(int color) {
        return String.format("#%06X", color);
    }

    public static Color inverse(Color color) {
        return new Color(
                255 - color.getRed(),
                255 - color.getGreen(),
                255 - color.getBlue(),
                color.getAlpha());
    }

    public static int inverse(int color) {
        int a = color >> 24 & 255;
        int r = color >> 16 & 255;
        int g = color >> 8 & 255;
        int b = color & 255;
        return (a << 24) | ((255 - r) << 16) | ((255 - g) << 8) | (255 - b);
    }

    public static int shadowed(int color, float factor) {
        int r = ColorUtils.Int.r(color);
        int g = ColorUtils.Int.g(color);
        int b = ColorUtils.Int.b(color);
        int a = ColorUtils.Int.a(color);
        r = Math.round(r * factor);
        g = Math.round(g * factor);
        b = Math.round(b * factor);
        return ColorUtils.Int.combine(r, g, b, a);
    }

    public static int toShader(int color) {
        int a = Int.a(color);
        int r = Int.r(color);
        int g = Int.g(color);
        int b = Int.b(color);
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    public static final class Int {

        public static int r(int color) {
            return (color >>> 16) & 0xFF;
        }

        public static int g(int color) {
            return (color >>> 8) & 0xFF;
        }

        public static int b(int color) {
            return color & 0xFF;
        }

        public static int a(int color) {
            return (color >>> 24) & 0xFF;
        }

        public static int combine(int r, int g, int b, int a) {
            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        public static int multiply(int color1, int color2) {
            return combine(
                    r(color1) * r(color2) / 255,
                    g(color1) * g(color2) / 255,
                    b(color1) * b(color2) / 255,
                    a(color1) * a(color2) / 255);
        }
    }
}