package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;
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

    @ApiVisibility(ApiType.ACTION)
    public void setXRot(double value) {
        if (mc.player == null) {
            return;
        }
        mc.player.setXRot((float)value);
    }

    @ApiVisibility(ApiType.ACTION)
    public void setYRot(double value) {
        if (mc.player == null) {
            return;
        }
        mc.player.setYRot((float)value);
    }

    @ApiVisibility(ApiType.ACTION)
    public void lookAt(double x, double y, double z) {
        if (mc.player == null) {
            return;
        }
        RotationUtils.Rotation rotation = RotationUtils.getRotation(mc.player.getEyePosition(), new Vec3(x, y, z));
        mc.player.setXRot(rotation.xRot());
        mc.player.setYRot(rotation.yRot());
    }
}