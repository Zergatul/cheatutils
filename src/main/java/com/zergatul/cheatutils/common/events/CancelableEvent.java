package com.zergatul.cheatutils.common.events;

public interface CancelableEvent {
    void cancel();
    boolean isCanceled();
}