package com.zergatul.cheatutils.utils;

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
            return Integer.parseInt(str.substring(1, 3), 16) << 16 | Integer.parseInt(str.substring(3, 5), 16) << 8 | Integer.parseInt(str.substring(5, 7), 16);
        }
        return null;
    }
}