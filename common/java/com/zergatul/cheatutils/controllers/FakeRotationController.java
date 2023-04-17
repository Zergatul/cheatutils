package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.utils.RotationUtils;
import net.minecraft.client.Minecraft;
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
        RotationUtils.Rotation rotation = RotationUtils.getRotation(mc.player.getEyePosition(), pos);
        setServerRotation(rotation.xRot(), rotation.yRot());
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
