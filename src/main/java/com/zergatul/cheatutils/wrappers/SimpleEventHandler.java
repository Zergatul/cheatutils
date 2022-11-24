package com.zergatul.cheatutils.wrappers;

import java.util.ArrayList;
import java.util.List;

public class SimpleEventHandler {

    private final List<Runnable> handlers = new ArrayList<>();

    public void add(Runnable handler) {
        handlers.add(handler);
    }

    public void trigger() {
        for (Runnable handler: handlers) {
            handler.run();
        }
    }
}