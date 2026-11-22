package com.zergatul.cheatutils.configs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigWriterQueueTests {

    private static final long NEVER = TimeUnit.DAYS.toNanos(1);

    @TempDir
    Path directory;

    private final ConfigWriterQueue queue = new ConfigWriterQueue();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @AfterEach
    public void cleanup() {
        executor.shutdownNow();
        queue.close();
    }

    @Test
    public void newerRequestReplacesPendingWriteAndKeepsItsDeadline() throws Exception {
        File file = file("profile.json");
        List<String> writes = new CopyOnWriteArrayList<>();
        CountDownLatch written = new CountDownLatch(1);

        queue.queue(file, TimeUnit.MILLISECONDS.toNanos(50), () -> writes.add("old"));
        queue.queue(file, NEVER, () -> {
            writes.add("new");
            written.countDown();
        });

        assertTrue(written.await(5, TimeUnit.SECONDS));
        queue.flush(file);
        assertEquals(List.of("new"), writes);
    }

    @Test
    public void flushWaitsForRunningWriteAndThenWritesLatest() throws Exception {
        File file = file("profile.json");
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try {
            queue.queue(file, 0, () -> {
                started.countDown();
                await(release);
                write(file, "old");
            });
            assertTrue(started.await(5, TimeUnit.SECONDS));
            queue.queue(file, NEVER, () -> write(file, "new"));

            Future<?> flush = executor.submit(() -> queue.flush(file));
            assertThrows(TimeoutException.class, () -> flush.get(100, TimeUnit.MILLISECONDS));
            release.countDown();
            flush.get(5, TimeUnit.SECONDS);

            assertEquals("new", Files.readString(file.toPath()));
        } finally {
            release.countDown();
        }
    }

    @Test
    public void deletedFileIsNotRecreatedByItsPendingWrite() throws Exception {
        File file = file("profile.json");
        write(file, "saved");
        queue.queue(file, NEVER, () -> write(file, "pending"));

        assertTrue(queue.delete(file));
        queue.flush(file);
        queue.close();

        assertFalse(file.exists());
    }

    @Test
    public void disableWaitsForRunningWriteAndIgnoresLaterRequests() throws Exception {
        File file = file("profile.json");
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try {
            queue.queue(file, 0, () -> {
                started.countDown();
                await(release);
                write(file, "running");
            });
            assertTrue(started.await(5, TimeUnit.SECONDS));
            queue.queue(file, NEVER, () -> write(file, "pending"));

            Future<?> disable = executor.submit(queue::disable);
            assertThrows(TimeoutException.class, () -> disable.get(100, TimeUnit.MILLISECONDS));
            release.countDown();
            disable.get(5, TimeUnit.SECONDS);

            Files.delete(file.toPath());
            queue.queue(file, 0, () -> write(file, "after reset"));
            queue.flush(file);
            queue.close();

            assertFalse(file.exists());
        } finally {
            release.countDown();
        }
    }

    @Test
    public void closeWritesPendingWritesNow() throws Exception {
        File first = file("first.json");
        File second = file("second.json");
        queue.queue(first, NEVER, () -> write(first, "1"));
        queue.queue(second, NEVER, () -> write(second, "2"));

        queue.close();

        assertEquals("1", Files.readString(first.toPath()));
        assertEquals("2", Files.readString(second.toPath()));
        queue.queue(first, 0, () -> write(first, "after close"));
        assertEquals("1", Files.readString(first.toPath()));
    }

    @Test
    public void failingWriteDoesNotStopTheWriter() throws Exception {
        File file = file("profile.json");
        queue.queue(file, NEVER, () -> {
            throw new IllegalStateException("broken");
        });
        queue.flush(file);

        queue.queue(file, NEVER, () -> write(file, "next"));
        queue.flush(file);

        assertEquals("next", Files.readString(file.toPath()));
    }

    @Test
    public void writeAtomicallyReplacesContentWithoutLeavingTemporaryFile() throws Exception {
        File file = file("profile.json");
        write(file, "old");

        ConfigWriterQueue.writeAtomically(file, "new");

        assertEquals("new", Files.readString(file.toPath()));
        try (var files = Files.list(directory)) {
            assertEquals(List.of(file.toPath()), files.toList());
        }
    }

    private File file(String name) {
        return directory.resolve(name).toFile();
    }

    private static void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    private static void write(File file, String text) {
        try {
            Files.writeString(file.toPath(), text);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
