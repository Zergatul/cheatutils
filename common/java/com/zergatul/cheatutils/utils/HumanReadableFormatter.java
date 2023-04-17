package com.zergatul.cheatutils.utils;

public class HumanReadableFormatter {
    public static String formatLong(long value) {
        if (value < 1000) {
            return Long.toString(value);
        }

        int thousands = (int)Math.round(1d * value / 1000);
        if (thousands < 1000) {
            return thousands + "k";
        }

        int millions = (int)Math.round(1d * value / 1000000);
        if (millions < 1000) {
            return millions + "m";
        }

        int billions = (int)Math.round(1d * value / 1000000000);
        return billions + "b";
    }
}
