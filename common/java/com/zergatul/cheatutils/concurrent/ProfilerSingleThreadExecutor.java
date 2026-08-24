package com.zergatul.cheatutils.concurrent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ProfilerSingleThreadExecutor extends ThreadPoolExecutor {

    private final Logger logger = LogManager.getLogger(ProfilerSingleThreadExecutor.class);
    private final BusyCounter counter = new BusyCounter();
    private volatile int successful;
    private volatile int failed;
    private volatile int rejected;

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
        if (throwable == null) {
            successful++;
        } else {
            failed++;
            logger.error("ProfilerSingleThreadExecutor exception", throwable);
        }
        super.afterExecute(runnable, throwable);
        counter.startWait();
    }

    private static void onRejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
        ((ProfilerSingleThreadExecutor) executor).rejected++;
    }
}