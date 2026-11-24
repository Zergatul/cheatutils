package com.zergatul.cheatutils.common.events;

public class PlayerReleaseUsingItemEvent implements CancelableEvent {

    private boolean canceled;

    @Override
    public void cancel() {
        canceled = true;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }
}