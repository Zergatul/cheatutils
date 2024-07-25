package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.utils.MathUtils;

import java.util.Locale;

public class ConvertApi {

    public int toInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return Integer.MIN_VALUE;
        }
    }

    public double toFloat(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    public String toString(int value) {
        return Integer.toString(value);
    }

    public String toString(double value) {
        return String.format(Locale.ROOT, "%s", value);
    }

    public String toString(double value, int decimals) {
        decimals = MathUtils.clamp(decimals, 0, 20);
        return String.format(Locale.ROOT, "%." + decimals + "f", value);
    }
}