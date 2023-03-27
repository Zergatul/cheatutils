package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.utils.MathUtils;

public class ColorApi {

    public String toHex(int red, int green, int blue) {
        red = MathUtils.clamp(red, 0, 255);
        green = MathUtils.clamp(green, 0, 255);
        blue = MathUtils.clamp(blue, 0, 255);
        return String.format("#%02X%02X%02X", red, green, blue);
    }
}