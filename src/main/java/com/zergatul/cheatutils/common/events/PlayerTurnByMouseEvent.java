package com.zergatul.cheatutils.common.events;

public class PlayerTurnByMouseEvent implements CancelableEvent {

    private final double xRot;
    private final double yRot;
    private boolean canceled;

    public PlayerTurnByMouseEvent(double xRot, double yRot) {
        this.xRot = xRot;
        this.yRot = yRot;
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