package com.zergatul.cheatutils.scripting.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

public class UIApi {
    /** Local output only; never sends a chat packet to the server. */
    public void systemMessage(String text) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(text));
    }
}
