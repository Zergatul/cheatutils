package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.utils.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

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

    public void lookAt(double x, double y, double z) {
        if (mc.player == null) {
            return;
        }
        RotationUtils.Rotation rotation = RotationUtils.getRotation(mc.player.getEyePosition(), new Vec3(x, y, z));
        mc.player.setXRot(rotation.xRot());
        mc.player.setYRot(rotation.yRot());
    }
}