package com.zergatul.cheatutils.scripting.api.modules;

import net.minecraft.client.Minecraft;

public class FontApi {

    public int getStringWidth(String value) {
        return Minecraft.getInstance().font.width(value);
    }

    public int getLineHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }
}