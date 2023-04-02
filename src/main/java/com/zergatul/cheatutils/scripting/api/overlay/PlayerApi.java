package com.zergatul.cheatutils.scripting.api.overlay;

import net.minecraft.client.MinecraftClient;

public class PlayerApi {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public double getX() {
        if (mc.player == null) {
            return 0;
        } else {
            return mc.player.getX();
        }
    }

    public double getY() {
        if (mc.player == null) {
            return 0;
        } else {
            return mc.player.getY();
        }
    }

    public double getZ() {
        if (mc.player == null) {
            return 0;
        } else {
            return mc.player.getZ();
        }
    }

}
