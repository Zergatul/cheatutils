package com.zergatul.cheatutils.wrappers;

import com.zergatul.cheatutils.wrappers.events.CancelableEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CancelableEventHandler<T extends CancelableEvent> {

    private final List<Consumer<T>> handlers = new ArrayList<>();

    public void add(Consumer<T> handler) {
        handlers.add(handler);
    }

    public boolean trigger(T parameter) {
        for (Consumer<T> handler: handlers) {
            handler.accept(parameter);
            if (parameter.isCanceled()) {
                return true;
            }
        }
        return false;
    }
}