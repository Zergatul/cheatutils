package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.scripting.MethodDescription;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;

public class CameraApi {

    private static final Minecraft mc = Minecraft.getInstance();

    public double getX() {
        if (mc.level == null) {
            return Double.NaN;
        }

        Camera camera = mc.gameRenderer.mainCamera();
        return camera.position().x;
    }

    public double getY() {
        if (mc.level == null) {
            return Double.NaN;
        }

        Camera camera = mc.gameRenderer.mainCamera();
        return camera.position().y;
    }

    public double getZ() {
        if (mc.level == null) {
            return Double.NaN;
        }

        Camera camera = mc.gameRenderer.mainCamera();
        return camera.position().z;
    }

    @MethodDescription("Camera pitch in degrees.")
    public double getXRot() {
        if (mc.level == null) {
            return Double.NaN;
        }

        Camera camera = mc.gameRenderer.mainCamera();
        return camera.xRot();
    }

    @MethodDescription("Camera yaw in degrees.")
    public double getYRot() {
        if (mc.level == null) {
            return Double.NaN;
        }

        Camera camera = mc.gameRenderer.mainCamera();
        return camera.yRot();
    }
}