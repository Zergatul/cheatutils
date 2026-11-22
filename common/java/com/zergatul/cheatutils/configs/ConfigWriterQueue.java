package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.Events;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Delayed config writes. One writer thread performs every disk operation, so writes of the same file never overlap
 * and run in request order. {@link #flush}, {@link #delete}, {@link #disable} and {@link #close} wait until the
 * writer has finished everything that was requested before them.
 * <p>
 * At most one write is pending per file: a newer request replaces the pending write and keeps its deadline,
 * so a file is saved at most once per delay, no later than the delay after its first unsaved change.
 * <p>
 * Write actions do only I/O: they never take the store lock or wait for another thread, so any thread may wait
 * for the writer. Waiting while holding the store lock would block every settings update for the duration of
 * the I/O, so callers don't do that.
 */
public class ConfigWriterQueue {

    public static final ConfigWriterQueue INSTANCE = new ConfigWriterQueue(true);

    private final Logger logger = LogManager.getLogger(ConfigWriterQueue.class);
    private final ScheduledThreadPoolExecutor writer;
    private final Map<File, Runnable> pending = new LinkedHashMap<>(); // guarded by itself
    private boolean disabled; // guarded by pending

    ConfigWriterQueue() {
        this(false);
    }

    private ConfigWriterQueue(boolean registerLifecycleHandler) {
        writer = new ScheduledThreadPoolExecutor(1, action -> {
            Thread thread = new Thread(action, Constants.MOD_ID + " config writer");
            // never keeps the game alive; a normal exit saves everything in close()
            thread.setDaemon(true);
            return thread;
        });
        // after close() the timers have nothing left to write
        writer.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);

        if (registerLifecycleHandler) {
            Events.Close.add(this::close);
        }
    }

    /** Writes file after the delay, unless a newer request for the same file replaces this one first. */
    public void queue(File file, long delayNanos, Runnable write) {
        synchronized (pending) {
            if (disabled) {
                return;
            }
            if (pending.put(file, write) == null) {
                writer.schedule(() -> runPending(file), delayNanos, TimeUnit.NANOSECONDS);
            }
        }
    }

    /** Writes the pending write of file now. Returns when file on disk is up to date with every earlier request. */
    public void flush(File file) {
        runAndWait(() -> runPending(file));
    }

    /** Deletes file and drops its pending write, so an earlier request can't recreate it. Returns false on failure. */
    public boolean delete(File file) {
        return runAndWait(() -> {
            synchronized (pending) {
                pending.remove(file);
            }
            return !file.exists() || file.delete();
        });
    }

    /**
     * Drops all pending writes and ignores later requests. Returns after a write that was already running has
     * finished, so the caller can delete config files without them being recreated.
     */
    public void disable() {
        synchronized (pending) {
            disabled = true;
            pending.clear();
        }
        runAndWait(() -> null);
    }

    /** Game shutdown: writes everything that is pending, ignores later requests and stops the writer. */
    void close() {
        if (writer.isShutdown()) {
            return;
        }
        synchronized (pending) {
            disabled = true;
        }
        runAndWait(() -> {
            List<File> files;
            synchronized (pending) {
                files = new ArrayList<>(pending.keySet());
            }
            files.forEach(this::runPending);
            return null;
        });
        writer.shutdown();
    }

    /**
     * Replaces file with content. The content goes to a temporary file first, so a failed or interrupted write
     * leaves the previous file intact.
     */
    public static void writeAtomically(File file, String content) throws IOException {
        Path target = file.toPath();
        Path temp = target.resolveSibling(target.getFileName() + ".tmp");
        Files.writeString(temp, content);
        try {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // writer thread
    private void runPending(File file) {
        Runnable write;
        synchronized (pending) {
            write = pending.remove(file);
        }
        // a flushed write leaves its timer behind: it finds nothing, or saves a newer request early
        if (write != null) {
            try {
                write.run();
            } catch (RuntimeException e) {
                logger.error("Cannot write {}", file.getName(), e);
            }
        }
    }

    private void runAndWait(Runnable action) {
        runAndWait(() -> {
            action.run();
            return null;
        });
    }

    private <T> T runAndWait(Callable<T> action) {
        Future<T> future;
        try {
            future = writer.submit(action);
        } catch (RejectedExecutionException e) {
            throw new IllegalStateException("Config writer is closed.", e);
        }

        boolean interrupted = false;
        try {
            while (true) {
                try {
                    return future.get();
                } catch (InterruptedException e) {
                    // the operation is already queued: finish waiting, so the barrier still holds
                    interrupted = true;
                } catch (ExecutionException e) {
                    throw new IllegalStateException("Config I/O failed.", e.getCause());
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}