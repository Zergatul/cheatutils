package com.zergatul.cheatutils.common.events;

import net.minecraft.client.entity.EntityPlayerSP;

public class PlayerTurnByMouseEvent implements CancelableEvent {

    private final EntityPlayerSP player;
    private final double xRot, yRot;
    private boolean canceled;

    public PlayerTurnByMouseEvent(EntityPlayerSP player, double xRot, double yRot) {
        this.player = player;
        this.xRot = xRot;
        this.yRot = yRot;
    }

    public EntityPlayerSP getPlayer() {
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