package com.zergatul.cheatutils.scripting.modules;

import net.minecraft.client.Minecraft;

public class ClipboardApi {

    public String get() {
        return Minecraft.getInstance().keyboardHandler.getClipboard();
    }

    public void set(String value) {
        Minecraft.getInstance().keyboardHandler.setClipboard(value);
    }
}