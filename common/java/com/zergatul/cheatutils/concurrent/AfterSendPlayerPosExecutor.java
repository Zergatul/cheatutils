package com.zergatul.cheatutils.concurrent;

import com.zergatul.cheatutils.common.Events;
import org.jetbrains.annotations.NotNull;

public class AfterSendPlayerPosExecutor extends EventExecutor {

    public static final AfterSendPlayerPosExecutor instance = new AfterSendPlayerPosExecutor();

    private boolean insideEvent;

    private AfterSendPlayerPosExecutor() {
        super(5000);
        Events.AfterSendPlayerPos.add(this::onAfterSendPlayerPos, 1000);
    }

    @Override
    public void execute(@NotNull Runnable command) {
        if (insideEvent) {
            command.run();
        } else {
            super.execute(command);
        }
    }

    private void onAfterSendPlayerPos() {
        insideEvent = true;
        processQueue();
        insideEvent = false;
    }
}