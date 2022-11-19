package com.zergatul.cheatutils.controllers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class FakeRotationController {

    public static final FakeRotationController instance = new FakeRotationController();

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private boolean fake;
    private float serverXRot, serverYRot;
    private float clientXRot, clientYRot;

    private FakeRotationController() {
        PlayerMotionController.instance.addOnBeforeSendPosition(this::onBeforeSendPosition);
        PlayerMotionController.instance.addOnAfterSendPosition(this::onAfterSendPosition);
    }

    public void setServerRotation(Vec3d pos) {
        ClientPlayerEntity player = mc.player;
        Vec3d eyePos = new Vec3d(player.getX(), player.getY() + player.getStandingEyeHeight(), player.getZ());
        Vec3d diff = pos.subtract(eyePos);
        double diffXZ = Math.sqrt(diff.x * diff.x + diff.y * diff.y);
        float xRot = (float)Math.toDegrees(-Math.atan2(diff.y, diffXZ));
        float yRot = (float)Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90F;
        setServerRotation(xRot, yRot);
    }

    public void setServerRotation(float xRot, float yRot) {
        fake = true;
        serverXRot = xRot;
        serverYRot = yRot;
    }

    private void onBeforeSendPosition() {
        if (!fake) {
            return;
        }

        clientXRot = mc.player.getPitch();
        clientYRot = mc.player.getYaw();
        mc.player.setPitch(serverXRot);
        mc.player.setYaw(serverYRot);
    }

    private void onAfterSendPosition() {
        if (!fake) {
            return;
        }

        mc.player.setPitch(clientXRot);
        mc.player.setYaw(clientYRot);
        fake = false;
    }
}