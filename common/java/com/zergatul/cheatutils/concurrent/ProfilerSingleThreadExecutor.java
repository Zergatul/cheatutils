package com.zergatul.cheatutils.concurrent;

import java.util.concurrent.*;

public class ProfilerSingleThreadExecutor extends ThreadPoolExecutor {

    private final BusyCounter counter = new BusyCounter();
    private int successful;
    private int failed;
    private int rejected;

    public ProfilerSingleThreadExecutor(int capacity) {
        super(
                1, 1,
                0, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(capacity),
                Executors.defaultThreadFactory(),
                ProfilerSingleThreadExecutor::onRejectedExecution);
    }

    public double getBusyPercentage() {
        return 100d * counter.getLoad(1);
    }

    public int getQueueSize() {
        return getQueue().size();
    }

    public int getSuccessful() {
        return successful;
    }

    public int getFailed() {
        return failed;
    }

    public int getRejected() {
        return rejected;
    }

    @Override
    protected void beforeExecute(Thread thread, Runnable runnable) {
        counter.startLoad();
        super.beforeExecute(thread, runnable);
    }

    @Override
    protected void afterExecute(Runnable runnable, Throwable throwable) {
        if (throwable != null) {
            failed++;
        } else {
            successful++;
        }

        super.afterExecute(runnable, throwable);
        counter.startWait();
    }

    private static void onRejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
        ((ProfilerSingleThreadExecutor) executor).rejected++;
    }
}