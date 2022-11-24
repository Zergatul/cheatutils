package com.zergatul.cheatutils.wrappers.events;

public interface CancelableEvent {
    void cancel();
    boolean isCanceled();
}