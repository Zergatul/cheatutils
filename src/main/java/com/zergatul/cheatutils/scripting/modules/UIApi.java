package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

@SuppressWarnings("unused")
public class UIApi {

    private final Minecraft mc = Minecraft.getMinecraft();

    @ApiVisibility({ ApiType.ACTION, ApiType.LOGGING })
    public void systemMessage(String text) {
        mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentString(text));
    }
}