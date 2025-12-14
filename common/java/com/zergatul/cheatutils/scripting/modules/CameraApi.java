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
        return camera.getPosition().x;
    }

    public double getY() {
        if (mc.level == null) {
            return Double.NaN;
        }

        Camera camera = mc.gameRenderer.getMainCamera();
        return camera.getPosition().y;
    }

    public double getZ() {
        if (mc.level == null) {
            return Double.NaN;
        }

        Camera camera = mc.gameRenderer.getMainCamera();
        return camera.getPosition().z;
    }

    public double getXRot() {
        if (mc.level == null) {
            return Double.NaN;
        }

        Camera camera = mc.gameRenderer.getMainCamera();
        return camera.getXRot();
    }

    public double getYRot() {
        if (mc.level == null) {
            return Double.NaN;
        }

        Camera camera = mc.gameRenderer.getMainCamera();
        return camera.getYRot();
    }
}