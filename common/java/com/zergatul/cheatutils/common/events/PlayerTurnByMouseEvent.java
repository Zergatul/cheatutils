package com.zergatul.cheatutils.common.events;

import net.minecraft.client.player.LocalPlayer;

public class PlayerTurnByMouseEvent implements CancelableEvent {

    private final LocalPlayer player;
    private final double xRot, yRot;
    private boolean canceled;

    public PlayerTurnByMouseEvent(LocalPlayer player, double xRot, double yRot) {
        this.player = player;
        this.xRot = xRot;
        this.yRot = yRot;
    }

    public LocalPlayer getPlayer() {
        return player;
    }

    public double getXRot() {
        return xRot;
    }

    public double getYRot() {
        return yRot;
    }

    @Override
    public void cancel() {
        canceled = true;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }
}