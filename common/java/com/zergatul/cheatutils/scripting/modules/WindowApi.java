package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.scripting.MethodDescription;
import net.minecraft.client.Minecraft;

@SuppressWarnings("unused")
public class WindowApi {

    @MethodDescription("Width of Minecraft window drawing area, in Minecraft pixels, not real pixels")
    public int getGuiWidth() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    @MethodDescription("Height of Minecraft window drawing area, in Minecraft pixels, not real pixels")
    public int getGuiHeight() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }
}