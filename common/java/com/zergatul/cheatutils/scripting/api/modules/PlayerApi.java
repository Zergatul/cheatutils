package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.controllers.DisconnectController;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.HelpText;
import com.zergatul.cheatutils.utils.Rotation;
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
        return mc.player.getY();
    }

    public double getZ() {
        if (mc.player == null) {
            return 0;
        }
        return mc.player.getZ();
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

    public int getHealth() {
        if (mc.player == null) {
            return 0;
        }

        return (int) mc.player.getHealth();
    }

    public int getFood() {
        if (mc.player == null) {
            return 0;
        }

        return mc.player.getFoodData().getFoodLevel();
    }

    public boolean isUnderwater() {
        if (mc.player == null) {
            return false;
        }

        return mc.player.isUnderWater();
    }

    public boolean isElytraFlying() {
        if (mc.player == null) {
            return false;
        }

        return mc.player.isFallFlying();
    }

    public boolean isOnGround() {
        if (mc.player == null) {
            return false;
        }

        return mc.player.onGround();
    }

    public boolean isPassenger() {
        if (mc.player == null) {
            return false;
        }

        return mc.player.isPassenger();
    }

    @ApiVisibility(ApiType.ACTION)
    public void lookAt(double x, double y, double z) {
        if (mc.player == null) {
            return;
        }
        Rotation rotation = RotationUtils.getRotation(mc.player.getEyePosition(), new Vec3(x, y, z));
        mc.player.setXRot(rotation.xRot());
        mc.player.setYRot(rotation.yRot());
    }

    @HelpText("types: \"self-attack\", \"invalid-chars\", anything else - normal disconnect.")
    @ApiVisibility(ApiType.DISCONNECT)
    public void disconnect(String type) {
        switch (type) {
            case "self-attack" -> DisconnectController.instance.selfAttack(null);
            case "invalid-chars" -> DisconnectController.instance.invalidChars(null);
            default -> DisconnectController.instance.disconnect(null);
        }
    }

    @HelpText("message to be displayed on the disconnect screen")
    @ApiVisibility(ApiType.DISCONNECT)
    public void disconnect(String type, String message) {
        switch (type) {
            case "self-attack" -> DisconnectController.instance.selfAttack(message);
            case "invalid-chars" -> DisconnectController.instance.invalidChars(message);
            default -> DisconnectController.instance.disconnect(message);
        }
    }
}