package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.scripting.MethodDescription;
import net.minecraft.client.Minecraft;

@SuppressWarnings("unused")
public class RenderApi {

    @MethodDescription("""
            Returns vanilla-calculated FPS
            """)
    public int getFps() {
        return Minecraft.getInstance().getFps();
    }

    @MethodDescription("""
            Returns vanilla-calculated frametime in ms
            """)
    public double getFrameTime() {
        return Minecraft.getInstance().getFrameTimeNs() / 1e6;
    }
}