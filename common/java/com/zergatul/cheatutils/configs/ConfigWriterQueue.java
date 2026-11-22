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
 * At most one write is pending per file, a newer request replaces it. The file is saved once it had no new
 * requests for {@link #QUIET_DELAY}, but no later than {@link #MAX_DELAY} after its first unsaved request.
 * Edits from the UI are saved shortly after the user stops; a script that changes settings all the time causes
 * one write per {@link #MAX_DELAY}, not one per change.
 * <p>
 * Write actions do only I/O: they never take the store lock or wait for another thread, so any thread may wait
 * for the writer. Waiting while holding the store lock would block every settings update for the duration of
 * the I/O, so callers don't do that.
 */
public class ConfigWriterQueue {

    static final long QUIET_DELAY = TimeUnit.SECONDS.toNanos(15);
    static final long MAX_DELAY = TimeUnit.MINUTES.toNanos(5);

    public static final ConfigWriterQueue INSTANCE = new ConfigWriterQueue(QUIET_DELAY, MAX_DELAY, true);

    private final Logger logger = LogManager.getLogger(ConfigWriterQueue.class);
    private final long quietDelay;
    private final long maxDelay;
    private final ScheduledThreadPoolExecutor writer;
    private final Map<File, Pending> pending = new LinkedHashMap<>(); // guarded by itself
    private boolean disabled; // guarded by pending

    ConfigWriterQueue(long quietDelay, long maxDelay) {
        this(quietDelay, maxDelay, false);
    }

    private ConfigWriterQueue(long quietDelay, long maxDelay, boolean registerLifecycleHandler) {
        this.quietDelay = quietDelay;
        this.maxDelay = maxDelay;
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

    /** Requests a write of file. Replaces the pending write of the same file and postpones it, up to the limit. */
    public void queue(File file, Runnable write) {
        synchronized (pending) {
            if (disabled) {
                return;
            }

            long now = System.nanoTime();
            Pending entry = pending.get(file);
            if (entry == null) {
                entry = new Pending(write, now);
                pending.put(file, entry);
                schedule(file, entry, Math.min(quietDelay, maxDelay));
            } else {
                // no new timer: the entry's timer sees the later deadline when it fires
                entry.write = write;
                entry.lastRequest = now;
            }
        }
    }

    /** Writes the pending write of file now. Returns when file on disk is up to date with every earlier request. */
    public void flush(File file) {
        runAndWait(() -> flushOnWriter(file));
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
            files.forEach(this::flushOnWriter);
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

    // under pending
    private void schedule(File file, Pending entry, long delay) {
        writer.schedule(() -> onTimer(file, entry), delay, TimeUnit.NANOSECONDS);
    }

    // writer thread
    private void onTimer(File file, Pending entry) {
        Runnable write;
        synchronized (pending) {
            if (pending.get(file) != entry) {
                return; // flushed, deleted or dropped; a newer entry has its own timer
            }

            long deadline = Math.min(entry.lastRequest + quietDelay, entry.firstRequest + maxDelay);
            long remaining = deadline - System.nanoTime();
            if (remaining > 0) {
                schedule(file, entry, remaining);
                return;
            }

            pending.remove(file);
            write = entry.write;
        }
        run(file, write);
    }

    // writer thread
    private void flushOnWriter(File file) {
        Pending entry;
        synchronized (pending) {
            entry = pending.remove(file);
        }
        if (entry != null) {
            run(file, entry.write);
        }
    }

    // writer thread
    private void run(File file, Runnable write) {
        try {
            write.run();
        } catch (RuntimeException e) {
            logger.error("Cannot write {}", file.getName(), e);
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

    // guarded by pending
    private static final class Pending {

        private final long firstRequest;
        private Runnable write;
        private long lastRequest;

        private Pending(Runnable write, long now) {
            this.write = write;
            this.firstRequest = now;
            this.lastRequest = now;
        }
    }
}