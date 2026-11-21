package com.zergatul.cheatutils.concurrent;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * The client (render) thread owns all script runtime state: installed programs, script documents,
 * Events callbacks, keybinding assignments and ESP script maps. Other threads change it only through
 * {@link #call}, which runs the whole operation on the client thread at the end of a client tick.
 * <p>
 * Rules that keep this deadlock-free:
 * <ul>
 *     <li>never run user script code while holding a lock;</li>
 *     <li>never wait for the client thread while holding a lock;</li>
 *     <li>only threads the client thread never waits for (web and MCP request threads) may wait for it.
 *     Code that can run on the Netty thread, like script callbacks, uses {@link #post} without waiting.</li>
 * </ul>
 */
public final class ClientThread {

    private static final Logger LOGGER = LogManager.getLogger(ClientThread.class);

    private ClientThread() {}

    public static boolean isCurrent() {
        Minecraft mc = Minecraft.getInstance();
        // null only in headless unit tests, where the test thread plays the client thread
        return mc == null || mc.isSameThread();
    }

    public static void assertCurrent() {
        if (!isCurrent()) {
            throw new IllegalStateException("Must be called from the client thread.");
        }
    }

    /**
     * Runs action on the client thread and returns its result. Runs inline when already on the client thread,
     * otherwise at the end of the next client tick while the calling thread waits.
     */
    public static <T> T call(Supplier<T> action) {
        if (isCurrent()) {
            return action.get();
        }

        return await(ClientTickEndExecutor.INSTANCE.submit(action::get));
    }

    public static void run(Runnable action) {
        call(() -> {
            action.run();
            return null;
        });
    }

    /**
     * Schedules action at the end of the current or next client tick, never runs it inline. Failures are logged,
     * so callers that don't care about the result can ignore the returned future.
     */
    public static CompletableFuture<Void> post(Runnable action) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        ClientTickEndExecutor.INSTANCE.execute(() -> {
            try {
                action.run();
                result.complete(null);
            } catch (RuntimeException e) {
                LOGGER.error("Client thread task failed", e);
                result.completeExceptionally(e);
            }
        });
        return result;
    }

    /** Waits for work scheduled on the client thread. The same rules as for {@link #call} apply. */
    public static <T> T await(Future<T> future) {
        if (isCurrent()) {
            throw new IllegalStateException("The client thread cannot wait for its own tasks.");
        }

        if (ConfigStore.INSTANCE.isLockHeldByCurrentThread()) {
            throw new IllegalStateException("Cannot wait for the client thread while holding the config store lock.");
        }

        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for the client thread.", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException(cause);
        }
    }
}
