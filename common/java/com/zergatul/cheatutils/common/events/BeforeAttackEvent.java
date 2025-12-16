package com.zergatul.cheatutils.common.events;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class BeforeAttackEvent implements CancelableEvent {

    private boolean canceled;
    private CallbackInfo callback;
    public BeforeAttackEvent(CallbackInfo ci) {
        this.callback = ci;
        canceled = false;
    }

    @Override
    public void cancel() {
        canceled = true;
        callback.cancel();
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }
}