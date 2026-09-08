package com.zergatul.cheatutils;

import com.zergatul.cheatutils.configs.*;
import com.zergatul.cheatutils.utils.ResourceHelper;
import com.zergatul.cheatutils.webui.StaticFilesHandler;
import com.zergatul.cheatutils.webui.LocalFilesHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** Java 8 checks without a Minecraft client or additional testing dependencies. */
public class InfrastructureChecks {
    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("cheatutils-checks-");
        try {
            checkQueue(root);
            checkFreeCamConfig();
            checkEvents();
            checkSnapshots(root);
            checkFiles(root);
            System.out.println("Infrastructure checks passed (queue, snapshots, config recovery, HTTP files).");
        } finally {
            ConfigWriterQueue.instance.close();
            try (java.util.stream.Stream<Path> files = Files.walk(root)) {
                files.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                    try { Files.delete(path); } catch (IOException e) { throw new UncheckedIOException(e); }
                });
            }
        }
    }

    private static void checkQueue(Path root) {
        ConfigWriterQueue queue = new ConfigWriterQueue();
        AtomicInteger value = new AtomicInteger();
        File file = root.resolve("queue.json").toFile();
        long delay = TimeUnit.HOURS.toNanos(1);
        queue.queue(file, delay, () -> value.set(1));
        queue.queue(file, delay, () -> value.set(2));
        queue.flush(file);
        require(value.get() == 2, "Flush must save the latest entry.");
        queue.queue(file, delay, () -> value.set(3));
        queue.cancel(file);
        queue.flush(file);
        require(value.get() == 2, "Cancelled writes must not execute.");
        queue.queue(file, delay, () -> value.set(4));
        queue.close();
        queue.close();
        require(value.get() == 4, "Close must flush pending writes.");
    }

    private static void checkEvents() {
        com.zergatul.cheatutils.common.events.ParameterizedEventHandler<String> event =
                new com.zergatul.cheatutils.common.events.ParameterizedEventHandler<>();
        StringBuilder calls = new StringBuilder();
        event.add(value -> calls.append("second"), 1);
        event.add(calls::append, -1);
        event.add(value -> calls.append("third"), 1);
        event.trigger("first");
        require(calls.toString().equals("firstsecondthird"), "Events must preserve priority and registration order.");
        com.zergatul.cheatutils.common.events.CancelableEventHandler<com.zergatul.cheatutils.common.events.SimpleCancellableEvent> cancelable =
                new com.zergatul.cheatutils.common.events.CancelableEventHandler<>();
        cancelable.add(value -> { throw new AssertionError("Cancelled event continued."); }, 1);
        cancelable.add(com.zergatul.cheatutils.common.events.SimpleCancellableEvent::cancel, -1);
        require(cancelable.trigger(new com.zergatul.cheatutils.common.events.SimpleCancellableEvent()),
                "Cancellation must propagate to the publisher.");
    }

    private static void checkFreeCamConfig() {
        FreeCamConfig config = new FreeCamConfig();
        config.acceleration = Double.NaN;
        config.maxSpeed = Double.POSITIVE_INFINITY;
        config.slowdownFactor = -1;
        config.sanitize();
        require(config.acceleration == 50 && config.maxSpeed == 50 && config.slowdownFactor == 1e-9,
                "Invalid FreeCam settings must not poison camera coordinates.");
        Config old = ConfigStore.instance.gson.fromJson("{}", Config.class);
        old.sanitize();
        require(old.freeCamConfig != null && old.freeCamConfig.target,
                "Existing profiles must gain default FreeCam settings.");
    }

    private static void checkSnapshots(Path root) throws IOException {
        ConfigStore store = new ConfigStore();
        File first = root.resolve("first.json").toFile();
        File second = root.resolve("second.json").toFile();
        store.read(first);
        store.getConfig().coreConfig.port = 5010;
        store.requestWrite();
        store.switchFile(second);
        store.getConfig().coreConfig.port = 5020;
        store.requestWrite();
        ConfigWriterQueue.instance.flush(first);
        ConfigWriterQueue.instance.flush(second);
        store.read(first);
        require(store.getConfig().coreConfig.port == 5010, "Old profile snapshot must retain its values.");
        store.read(second);
        require(store.getConfig().coreConfig.port == 5020, "New profile must save independently.");
        Files.write(first.toPath(), "{\"coreConfig\":null}".getBytes(StandardCharsets.UTF_8));
        store.read(first);
        require(store.getConfig().coreConfig.port == 5005, "Null config must get defaults.");
        Files.write(first.toPath(), "{broken".getBytes(StandardCharsets.UTF_8));
        store.read(first);
        require(store.getConfig().coreConfig.port == 5005, "Invalid config must recover defaults.");
        try (java.util.stream.Stream<Path> files = Files.list(root)) {
            require(files.anyMatch(path -> path.getFileName().toString().startsWith("first.json.invalid-")),
                    "Invalid config must be preserved.");
        }
    }

    private static void checkFiles(Path root) throws Exception {
        Path local = Files.createDirectory(root.resolve("local"));
        Files.write(local.resolve("fallback.js"), "export default 1;".getBytes(StandardCharsets.UTF_8));
        Files.write(root.resolve("secret.txt"), new byte[] { 1 });
        require(ResourceHelper.openLocal(local, "../secret.txt") == null, "Traversal must be rejected.");
        require(ResourceHelper.openLocal(local, "..\\secret.txt") == null, "Windows traversal must be rejected.");
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/local/", new LocalFilesHandler(local));
        server.createContext("/", new StaticFilesHandler());
        server.start();
        try {
            String base = "http://127.0.0.1:" + server.getAddress().getPort();
            require(status(base + "/", "GET") == 200, "Packaged index must be served.");
            require(status(base + "/", "HEAD") == 200, "HEAD must be supported.");
            require(status(base + "/local/fallback.js", "GET") == 200, "Local fallback must be served.");
            require(status(base + "/local/%2e%2e/secret.txt", "GET") == 404, "Encoded traversal must be rejected.");
            require(status(base + "/missing", "GET") == 404, "Missing file must return 404.");
            require(status(base + "/", "POST") == 405, "Static POST must return 405.");
        } finally {
            server.stop(0);
        }
    }

    private static int status(String url, String method) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(3000);
        try {
            return connection.getResponseCode();
        } finally {
            connection.disconnect();
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
