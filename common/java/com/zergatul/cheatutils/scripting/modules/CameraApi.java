package com.zergatul.cheatutils.scripting.modules;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;

public class CameraApi {

    private static final Minecraft mc = Minecraft.getInstance();

    public double getX() {
        if (mc.level == null) {
            return Double.NaN;
        }

        Camera camera = mc.gameRenderer.getMainCamera();
        return camera.position().x;
    }

    public double getY() {
        if (mc.level == null) {
            return Double.NaN;
        }

        Camera camera = mc.gameRenderer.getMainCamera();
        return camera.position().y;
    }

    public double getZ() {
        if (mc.level == null) {
            return Double.NaN;
        }

        Camera camera = mc.gameRenderer.getMainCamera();
        return camera.position().z;
    }

    public double getXRot() {
        if (mc.level == null) {
            return Double.NaN;
        }

        Camera camera = mc.gameRenderer.getMainCamera();
        return camera.xRot();
    }

    public double getYRot() {
        if (mc.level == null) {
            return Double.NaN;
        }

        Camera camera = mc.gameRenderer.getMainCamera();
        return camera.yRot();
    }
}