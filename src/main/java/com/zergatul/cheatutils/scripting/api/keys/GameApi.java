package com.zergatul.cheatutils.scripting.api.keys;

import net.minecraft.client.Minecraft;

public class GameApi {

    private final Minecraft mc = Minecraft.getInstance();

    public int getTick() {
        if (mc.level == null) {
            return 0;
        }
        return (int) mc.level.getGameTime();
    }
}