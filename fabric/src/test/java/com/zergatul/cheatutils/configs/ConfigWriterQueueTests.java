package com.zergatul.cheatutils.configs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigWriterQueueTests {

    private static final long NEVER = TimeUnit.DAYS.toNanos(1);

    @TempDir
    Path directory;

    private final List<ConfigWriterQueue> queues = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @AfterEach
    public void cleanup() {
        executor.shutdownNow();
        queues.forEach(ConfigWriterQueue::close);
    }

    @Test
    public void newerRequestReplacesPendingWrite() throws Exception {
        ConfigWriterQueue queue = queue(millis(50), NEVER);
        File file = file("profile.json");
        List<String> writes = new CopyOnWriteArrayList<>();
        CountDownLatch written = new CountDownLatch(1);

        queue.queue(file, () -> writes.add("old"));
        queue.queue(file, () -> {
            writes.add("new");
            written.countDown();
        });

        assertTrue(written.await(5, TimeUnit.SECONDS));
        queue.flush(file);
        assertEquals(List.of("new"), writes);
    }

    @Test
    public void newerRequestPostponesWriteUntilRequestsStop() throws Exception {
        long quiet = millis(500);
        ConfigWriterQueue queue = queue(quiet, NEVER);
        File file = file("profile.json");
        long[] writtenAt = new long[1];
        CountDownLatch written = new CountDownLatch(1);

        long first = System.nanoTime();
        queue.queue(file, () -> {});
        Thread.sleep(200);
        long second = System.nanoTime();
        // the first write would be due already, so a slow machine can't tell the two apart
        Assumptions.assumeTrue(second - first < millis(400));
        queue.queue(file, () -> {
            writtenAt[0] = System.nanoTime();
            written.countDown();
        });

        assertTrue(written.await(5, TimeUnit.SECONDS));
        assertTrue(writtenAt[0] - second >= quiet);
    }

    @Test
    public void constantRequestsAreWrittenAfterMaxDelay() throws Exception {
        long max = millis(200);
        ConfigWriterQueue queue = queue(NEVER, max);
        File file = file("profile.json");
        long[] writtenAt = new long[1];
        CountDownLatch written = new CountDownLatch(1);

        long first = System.nanoTime();
        long end = first + TimeUnit.SECONDS.toNanos(5);
        while (written.getCount() > 0 && System.nanoTime() < end) {
            queue.queue(file, () -> {
                writtenAt[0] = System.nanoTime();
                written.countDown();
            });
            Thread.sleep(10);
        }

        assertEquals(0, written.getCount());
        assertTrue(writtenAt[0] - first >= max);
    }

    @Test
    public void flushWaitsForRunningWriteAndThenWritesLatest() throws Exception {
        ConfigWriterQueue queue = queue(0, NEVER);
        File file = file("profile.json");
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try {
            queue.queue(file, () -> {
                started.countDown();
                await(release);
                write(file, "old");
            });
            assertTrue(started.await(5, TimeUnit.SECONDS));
            queue.queue(file, () -> write(file, "new"));

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
        ConfigWriterQueue queue = queue(NEVER, NEVER);
        File file = file("profile.json");
        write(file, "saved");
        queue.queue(file, () -> write(file, "pending"));

        assertTrue(queue.delete(file));
        queue.flush(file);
        queue.close();

        assertFalse(file.exists());
    }

    @Test
    public void disableWaitsForRunningWriteAndIgnoresLaterRequests() throws Exception {
        ConfigWriterQueue queue = queue(0, NEVER);
        File file = file("profile.json");
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try {
            queue.queue(file, () -> {
                started.countDown();
                await(release);
                write(file, "running");
            });
            assertTrue(started.await(5, TimeUnit.SECONDS));
            queue.queue(file, () -> write(file, "pending"));

            Future<?> disable = executor.submit(queue::disable);
            assertThrows(TimeoutException.class, () -> disable.get(100, TimeUnit.MILLISECONDS));
            release.countDown();
            disable.get(5, TimeUnit.SECONDS);

            Files.delete(file.toPath());
            queue.queue(file, () -> write(file, "after reset"));
            queue.flush(file);
            queue.close();

            assertFalse(file.exists());
        } finally {
            release.countDown();
        }
    }

    @Test
    public void closeWritesPendingWritesNow() throws Exception {
        ConfigWriterQueue queue = queue(NEVER, NEVER);
        File first = file("first.json");
        File second = file("second.json");
        queue.queue(first, () -> write(first, "1"));
        queue.queue(second, () -> write(second, "2"));

        queue.close();

        assertEquals("1", Files.readString(first.toPath()));
        assertEquals("2", Files.readString(second.toPath()));
        queue.queue(first, () -> write(first, "after close"));
        assertEquals("1", Files.readString(first.toPath()));
    }

    @Test
    public void failingWriteDoesNotStopTheWriter() throws Exception {
        ConfigWriterQueue queue = queue(NEVER, NEVER);
        File file = file("profile.json");
        queue.queue(file, () -> {
            throw new IllegalStateException("broken");
        });
        queue.flush(file);

        queue.queue(file, () -> write(file, "next"));
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

    private ConfigWriterQueue queue(long quietDelay, long maxDelay) {
        ConfigWriterQueue queue = new ConfigWriterQueue(quietDelay, maxDelay);
        queues.add(queue);
        return queue;
    }

    private File file(String name) {
        return directory.resolve(name).toFile();
    }

    private static long millis(long millis) {
        return TimeUnit.MILLISECONDS.toNanos(millis);
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
