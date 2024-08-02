package com.zergatul.cheatutils.utils;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;

public class ComponentUtils {

    public static MutableComponent constructMessage(String[] parameters) {
        if (parameters.length == 0 || parameters.length % 2 != 0) {
            return constructMessage("");
        } else {
            MutableComponent component = constructMessage("");
            for (int i = 0; i < parameters.length; i += 2) {
                component = component.append(constructMessage(parameters[i], parameters[i + 1]));
            }
            return component;
        }
    }

    public static MutableComponent constructMessage(String text) {
        return MutableComponent.create(new PlainTextContents.LiteralContents(text));
    }

    public static MutableComponent constructMessage(String color, String text) {
        Integer colorInt = ColorUtils.parseColor(color);
        MutableComponent component = MutableComponent.create(new PlainTextContents.LiteralContents(text));
        if (colorInt != null) {
            component = component.withStyle(Style.EMPTY.withColor(colorInt));
        }
        return component;
    }

    public static MutableComponent constructMessage(String color1, String text1, String color2, String text2) {
        Integer color1Int = ColorUtils.parseColor(color1);
        Integer color2Int = ColorUtils.parseColor(color2);
        MutableComponent component1 = MutableComponent.create(new PlainTextContents.LiteralContents(text1));
        if (color1Int != null) {
            component1 = component1.withStyle(Style.EMPTY.withColor(color1Int));
        }
        MutableComponent component2 = MutableComponent.create(new PlainTextContents.LiteralContents(text2));
        if (color2Int != null) {
            component2 = component2.withStyle(Style.EMPTY.withColor(color2Int));
        }
        return component1.append(" ").append(component2);
    }
}