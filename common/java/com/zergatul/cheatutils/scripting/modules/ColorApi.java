package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.scripting.HelpText;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.cheatutils.utils.MathUtils;

import java.awt.*;

public class ColorApi {

    public String toHex(int red, int green, int blue) {
        red = MathUtils.clamp(red, 0, 255);
        green = MathUtils.clamp(green, 0, 255);
        blue = MathUtils.clamp(blue, 0, 255);
        return String.format("#%02X%02X%02X", red, green, blue);
    }

    @HelpText("value should be [0..1]")
    public String gradient(String color1, String color2, double value) {
        value = MathUtils.clamp(value, 0, 1);
        Color c1 = ColorUtils.parseColor2(color1);
        Color c2 = ColorUtils.parseColor2(color2);
        if (c1 == null || c2 == null) {
            return "";
        }

        return toHex(
                (int) Math.round(c1.getRed() + (c2.getRed() - c1.getRed()) * value),
                (int) Math.round(c1.getGreen() + (c2.getGreen() - c1.getGreen()) * value),
                (int) Math.round(c1.getBlue() + (c2.getBlue() - c1.getBlue()) * value));
    }
}