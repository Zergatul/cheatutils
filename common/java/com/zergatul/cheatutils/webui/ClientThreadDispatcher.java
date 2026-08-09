package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import net.minecraft.client.Minecraft;
import org.apache.http.HttpException;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ClientThreadDispatcher {

    private static final long TIMEOUT_MILLIS = 2_000;

    private ClientThreadDispatcher() {}

    public static <T> T call(Callable<T> task) throws HttpException {
        Minecraft minecraft = Minecraft.getInstance();
        return call(ClientTickEndExecutor.instance, minecraft.isSameThread(), task, TIMEOUT_MILLIS);
    }

    public static void run(Runnable task) throws HttpException {
        Objects.requireNonNull(task);
        call(() -> {
            task.run();
            return null;
        });
    }

    static <T> T call(ExecutorService executor, boolean sameThread, Callable<T> task, long timeoutMillis) throws HttpException {
        Objects.requireNonNull(executor);
        Objects.requireNonNull(task);
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException("Timeout must be positive.");
        }

        if (sameThread) {
            return invoke(task);
        }
        if (executor.isShutdown()) {
            throw new ApiException("Client thread is not accepting tasks", HttpResponseCodes.SERVICE_UNAVAILABLE);
        }

        Future<T> future;
        try {
            future = executor.submit(task);
        } catch (RejectedExecutionException | IllegalStateException e) {
            throw new ApiException("Client thread is not accepting tasks", HttpResponseCodes.SERVICE_UNAVAILABLE, e);
        }

        try {
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            future.cancel(false);
            Thread.currentThread().interrupt();
            throw new ApiException("Interrupted while waiting for the client thread", HttpResponseCodes.SERVICE_UNAVAILABLE, e);
        } catch (TimeoutException e) {
            future.cancel(false);
            throw new ApiException("Timed out waiting for the client thread", HttpResponseCodes.GATEWAY_TIMEOUT, e);
        } catch (ExecutionException e) {
            return throwFailure(e.getCause());
        }
    }

    private static <T> T invoke(Callable<T> task) throws HttpException {
        try {
            return task.call();
        } catch (Throwable e) {
            return throwFailure(e);
        }
    }

    private static <T> T throwFailure(Throwable throwable) throws HttpException {
        if (throwable instanceof HttpException e) {
            throw e;
        }
        if (throwable instanceof RuntimeException e) {
            throw e;
        }
        if (throwable instanceof Error e) {
            throw e;
        }
        throw new ApiException("Client-thread task failed", HttpResponseCodes.INTERNAL_SERVER_ERROR, throwable);
    }
}