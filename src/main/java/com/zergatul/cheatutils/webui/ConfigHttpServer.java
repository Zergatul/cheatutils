package com.zergatul.cheatutils.webui;

import com.sun.net.httpserver.HttpServer;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ConfigHttpServer implements AutoCloseable {
    public static final ConfigHttpServer instance = new ConfigHttpServer();
    private static final Logger LOGGER = LogManager.getLogger(ConfigHttpServer.class);
    private final ExecutorService executor = Executors.newFixedThreadPool(8, r -> daemon(r, "cheatutils HTTP"));
    private final ScheduledExecutorService lifecycle = Executors.newSingleThreadScheduledExecutor(r -> daemon(r, "cheatutils HTTP lifecycle"));
    private HttpServer server;
    private File gameDirectory;
    private int basePort;
    private volatile boolean closed;
    private ScheduledFuture<?> restart;

    public ConfigHttpServer() {
        Events.Close.add(this::onClose);
        Events.ConfigLoaded.add(this::onConfigLoaded);
    }

    private void onClose() { close(); }
    private void onConfigLoaded() { onConfigUpdated(); }

    public synchronized void start(File gameDirectory) {
        if (closed || server != null) return;
        this.gameDirectory = gameDirectory;
        bind(ConfigStore.instance.getConfig().coreConfig.port);
    }

    public synchronized void onConfigUpdated() {
        if (closed || gameDirectory == null) return;
        int port = ConfigStore.instance.getConfig().coreConfig.port;
        if (restart != null) restart.cancel(false);
        // Allow the response that changes the port to finish first.
        restart = lifecycle.schedule(() -> rebind(port), 250, TimeUnit.MILLISECONDS);
    }

    private synchronized void rebind(int port) {
        if (closed || (server != null && basePort == port)) return;
        if (server != null) {
            server.stop(1);
            server = null;
        }
        bind(port);
    }

    private void bind(int port) {
        basePort = port;
        for (int candidate = port; candidate <= Math.min(65535, port + 99); candidate++) {
            HttpServer next;
            try {
                next = HttpServer.create(new InetSocketAddress("127.0.0.1", candidate), 0);
            } catch (BindException e) {
                continue;
            } catch (IOException e) {
                LOGGER.error("Cannot start HTTP server", e);
                return;
            }
            try {
                next.createContext("/api/", new ApiHandler());
                next.createContext("/local/", new LocalFilesHandler(new File(gameDirectory, "mods").toPath()));
                next.createContext("/", new StaticFilesHandler());
                next.setExecutor(executor);
                next.start();
                server = next;
                LOGGER.info("HTTP server started at http://127.0.0.1:{}/", candidate);
                return;
            } catch (RuntimeException e) {
                next.stop(0);
                LOGGER.error("Cannot initialize HTTP server", e);
                return;
            }
        }
        LOGGER.error("Cannot find free HTTP port starting at {}", port);
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        lifecycle.shutdownNow();
        if (server != null) {
            server.stop(0);
            server = null;
        }
        executor.shutdownNow();
    }

    private static Thread daemon(Runnable runnable, String name) {
        Thread thread = new Thread(runnable, name);
        thread.setDaemon(true);
        return thread;
    }
}
