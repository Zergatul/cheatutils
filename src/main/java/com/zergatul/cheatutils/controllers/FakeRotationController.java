package com.zergatul.cheatutils.controllers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class FakeRotationController {

    public static final FakeRotationController instance = new FakeRotationController();

    private final Minecraft mc = Minecraft.getInstance();
    private boolean fake;
    private float serverXRot, serverYRot;
    private float clientXRot, clientYRot;

    private FakeRotationController() {
        PlayerMotionController.instance.addOnBeforeSendPosition(this::onBeforeSendPosition);
        PlayerMotionController.instance.addOnAfterSendPosition(this::onAfterSendPosition);
    }

    public void setServerRotation(Vec3 pos) {
        LocalPlayer player = mc.player;
        Vec3 eyePos = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
        Vec3 diff = pos.subtract(eyePos);
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

        clientXRot = mc.player.getXRot();
        clientYRot = mc.player.getYRot();
        mc.player.setXRot(serverXRot);
        mc.player.setYRot(serverYRot);
    }

    private void onAfterSendPosition() {
        if (!fake) {
            return;
        }

        mc.player.setXRot(clientXRot);
        mc.player.setYRot(clientYRot);
        fake = false;
    }

}
