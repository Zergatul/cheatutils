package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.utils.Rotation;
import com.zergatul.cheatutils.utils.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class FakeRotation {

    public static final FakeRotation instance = new FakeRotation();

    private final Minecraft mc = Minecraft.getInstance();
    private boolean fake;
    private float serverXRot, serverYRot;
    private float clientXRot, clientYRot;
    //private Runnable afterSent;

    private FakeRotation() {
        Events.ClientTickStart.add(this::onTickStart);
        Events.BeforeSendPlayerPos.add(this::onBeforeSendPosition);
        Events.AfterSendPlayerPos.add(this::onAfterSendPosition);
    }

    public void setServerRotation(Vec3 pos) {
        assert mc.player != null;

        Rotation rotation = RotationUtils.getRotation(mc.player.getEyePosition(), pos);
        setServerRotation(rotation.xRot(), rotation.yRot());
    }

    public void setServerRotation(Rotation rotation) {
        setServerRotation(rotation.xRot(), rotation.yRot());
    }

    public void setServerRotation(float xRot, float yRot) {
        fake = true;
        serverXRot = xRot;
        serverYRot = yRot;
    }

    private void onTickStart() {
        // have to reset everything to not run into an issue when player disconnects,
        // and handlers run on another join
        fake = false;
        //afterSent = null;
    }

    private void onBeforeSendPosition() {
        assert mc.player != null;

        if (!fake) {
            return;
        }

        clientXRot = mc.player.getXRot();
        clientYRot = mc.player.getYRot();
        mc.player.setXRot(serverXRot);
        mc.player.setYRot(serverYRot);
    }

    private void onAfterSendPosition() {
        assert mc.player != null;

        if (!fake) {
            return;
        }

        mc.player.setXRot(clientXRot);
        mc.player.setYRot(clientYRot);
        fake = false;

        //afterSent.run();
    }
}
