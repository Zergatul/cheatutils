package com.zergatul.cheatutils.concurrent;

import com.zergatul.cheatutils.common.Events;

public class TickEndExecutor extends EventExecutor {

    public static final TickEndExecutor instance = new TickEndExecutor();

    private TickEndExecutor() {
        super(100);
        Events.ClientTickEnd.add(this::onClientTickEnd);
    }

    private void onClientTickEnd() {
        processQueue();
    }
}