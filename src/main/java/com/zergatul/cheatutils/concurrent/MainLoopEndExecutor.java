package com.zergatul.cheatutils.concurrent;

import com.zergatul.cheatutils.common.Events;

public class MainLoopEndExecutor extends EventExecutor {

    public static final MainLoopEndExecutor instance = new MainLoopEndExecutor();

    private MainLoopEndExecutor() {
        super(100);
        Events.MainLoopFrameEnd.add(this::onMainLoopFrameEnd);
    }

    private void onMainLoopFrameEnd() {
        processQueue();
    }
}