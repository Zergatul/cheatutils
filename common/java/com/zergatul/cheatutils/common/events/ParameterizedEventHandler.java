package com.zergatul.cheatutils.common.events;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ParameterizedEventHandler<T> {

    private final List<Consumer<T>> handlers = new ArrayList<>();

    public void add(Consumer<T> handler) {
        handlers.add(handler);
    }

    public void trigger(T parameter) {
        for (Consumer<T> handler: handlers) {
            handler.accept(parameter);
        }
    }
}