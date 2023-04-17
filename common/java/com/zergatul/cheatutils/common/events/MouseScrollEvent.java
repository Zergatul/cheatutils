package com.zergatul.cheatutils.common.events;

public class MouseScrollEvent implements CancelableEvent {

    private double scrollDelta;
    private boolean canceled;

    public MouseScrollEvent(double scrollDelta) {
        this.scrollDelta = scrollDelta;
    }

    public double getScrollDelta() {
        return scrollDelta;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void cancel() {
        canceled = true;
    }
}