package com.zergatul.cheatutils.concurrent;

import com.zergatul.cheatutils.common.Events;
import org.jetbrains.annotations.NotNull;

public class AfterPlayerAiStepExecutor extends EventExecutor {

    public static final AfterPlayerAiStepExecutor instance = new AfterPlayerAiStepExecutor();

    private boolean insideEvent;

    private AfterPlayerAiStepExecutor() {
        super(5000);
        Events.AfterPlayerAiStep.add(this::onAfterPlayerAiStep, 1000);
    }

    @Override
    public void execute(@NotNull Runnable command) {
        if (insideEvent) {
            command.run();
        } else {
            super.execute(command);
        }
    }

    private void onAfterPlayerAiStep() {
        insideEvent = true;
        processQueue();
        insideEvent = false;
    }
}