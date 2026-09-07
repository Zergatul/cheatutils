package com.zergatul.cheatutils.webui;

import com.sun.net.httpserver.HttpServer;
import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.scripting.monaco.MonacoIntegration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ConfigHttpServer implements AutoCloseable {

    public static final ConfigHttpServer instance = new ConfigHttpServer();

    private static final Logger logger = LogManager.getLogger(ConfigHttpServer.class);
    private final ExecutorService executor = Executors.newFixedThreadPool(8, r -> daemon(r, Constants.MOD_ID + " HTTP"));
    private final ScheduledExecutorService lifecycle = Executors.newSingleThreadScheduledExecutor(r -> daemon(r, Constants.MOD_ID + " HTTP lifecycle"));
    private HttpServer server;
    private int basePort;
    private volatile boolean closed;
    private ScheduledFuture<?> restart;

    private ConfigHttpServer() {
        Events.Close.add(this::close);
        Events.ConfigLoaded.add(this::onConfigUpdated);
    }

    public synchronized void onConfigUpdated() {
        if (closed) {
            return;
        }

        int port = ConfigStore.instance.getConfig().coreConfig.port;
        if (restart != null) {
            restart.cancel(false);
        }

        // Allow the response that changes the port to finish first.
        restart = lifecycle.schedule(() -> rebind(port), 250, TimeUnit.MILLISECONDS);
    }

    public synchronized void start() {
        if (closed || server != null) {
            return;
        }

        bind(ConfigStore.instance.getConfig().coreConfig.port);
    }

    private synchronized void rebind(int port) {
        if (closed) {
            return;
        }
        if (server != null && basePort == port) {
            return;
        }

        if (server != null) {
            server.stop(1);
            server = null;
        }
        bind(port);
    }

    private void bind(int port) {
        basePort = port;

        boolean found = false;
        for (int candidate = port; candidate <= Math.min(65535, port + 99); candidate++) {
            if (isAvailable(candidate)) {
                found = true;
                break;
            }
        }

        if (!found) {
            logger.error("Cannot find free port for HTTP server. BasePort={}.", basePort);
            return;
        }

        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            logger.error("Cannot start HttpServer on port {}.", port, e);
            return;
        }

        new MonacoIntegration().attach(server, "/api/code/");
        server.createContext("/api/", new ApiHandler());
        server.createContext("/textures/", new TexturesHandler());
        server.createContext("/local/", new LocalFilesHandler());
        server.createContext("/", new StaticFilesHandler());

        server.setExecutor(executor);
        server.start();
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }

        closed = true;
        lifecycle.shutdownNow();
        if (server != null) {
            server.stop(0);
            server = null;
        }
        executor.shutdownNow();
    }

    private static boolean isAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    private static Thread daemon(Runnable runnable, String name) {
        Thread thread = new Thread(runnable, name);
        thread.setDaemon(true);
        return thread;
    }
}