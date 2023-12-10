package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.scripting.HelpText;
import net.minecraft.client.Minecraft;

public class WindowApi {

    @HelpText("Width of Minecraft window drawing area, in Minecraft pixels, not real pixels")
    public int getGuiWidth() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    @HelpText("Height of Minecraft window drawing area, in Minecraft pixels, not real pixels")
    public int getGuiHeight() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }
}