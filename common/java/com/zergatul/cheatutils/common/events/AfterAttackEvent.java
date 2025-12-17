package com.zergatul.cheatutils.common.events;

public class AfterAttackEvent implements CancelableEvent {

    private boolean canceled;

    public AfterAttackEvent() {
        canceled = false;
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