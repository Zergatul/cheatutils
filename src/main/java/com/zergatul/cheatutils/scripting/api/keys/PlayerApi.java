package com.zergatul.cheatutils.scripting.api.keys;

import net.minecraft.client.Minecraft;

public class PlayerApi {

    private final static Minecraft mc = Minecraft.getInstance();

    public double getX() {
        if (mc.player == null) {
            return 0;
        }
        return mc.player.getX();
    }

    public double getY() {
        if (mc.player == null) {
            return 0;
        }
        return mc.player.getX();
    }

    public double getZ() {
        if (mc.player == null) {
            return 0;
        }
        return mc.player.getX();
    }

    public double getXRot() {
        if (mc.player == null) {
            return 0;
        }
        return mc.player.getXRot();
    }

    public double getYRot() {
        if (mc.player == null) {
            return 0;
        }
        return mc.player.getYRot();
    }

    public void setXRot(double value) {
        if (mc.player == null) {
            return;
        }
        mc.player.setXRot((float)value);
    }

    public void setYRot(double value) {
        if (mc.player == null) {
            return;
        }
        mc.player.setYRot((float)value);
    }
}