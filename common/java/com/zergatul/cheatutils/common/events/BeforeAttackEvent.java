package com.zergatul.cheatutils.common.events;

public class BeforeAttackEvent implements CancelableEvent {

    private boolean canceled;

    public BeforeAttackEvent(){
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