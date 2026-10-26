package com.zergatul.cheatutils.web;

import com.sun.net.httpserver.HttpServer;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.modules.CoreConfig;
import com.zergatul.cheatutils.mcp.McpHttpHandler;
import com.zergatul.cheatutils.modules.utilities.Core;
import com.zergatul.cheatutils.scripting.monaco.Integration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WebServer {

    public static final WebServer INSTANCE = new WebServer();

    private final Logger logger = LogManager.getLogger(WebServer.class);
    // we may need more threads, but they will not eat CPU,
    // since most likely they will be waiting for Future to be completed in the main thread
    private final ExecutorService executor = Executors.newFixedThreadPool(8);
    private HttpServer server;
    private boolean closed;

    private WebServer() {
        Events.Close.add(this::onClose);
        Core.INSTANCE.getSettings().onChanged(this::onConfigUpdated);

        this.server = null;
        this.closed = false;
    }

    public synchronized void start() {
        if (this.closed) {
            return;
        }

        int basePort = Core.INSTANCE.getSettings().get().port();
        int port = 0;
        for (int i = 0; i < 100; i++) {
            if (isAvailable(basePort + i)) {
                port = basePort + i;
                break;
            }
        }

        if (port == 0) {
            logger.error("Cannot find free port for HTTP server. BasePort={}.", basePort);
            return;
        }

        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            logger.error("Cannot start HttpServer on port {}.", port);
            logger.error(e);
            return;
        }

        new Integration().attach(server, "/api/code/");
        server.createContext("/api/", new ApiHandler());
        server.createContext("/assets/", new AssetsHandler());
        server.createContext("/textures/", new TexturesHandler());
        server.createContext("/local/", new LocalFilesHandler());
        server.createContext("/llm/cheatutils-llm-guide.md", new LargeLanguageModelGuideHandler());
        server.createContext("/llm/cheatutils-api.txt", new ApiGenHandler());
        server.createContext("/mcp", new McpHttpHandler());
        server.createContext("/", new StaticFilesHandler());

        server.setExecutor(executor);
        server.start();

        logger.info("HTTP server started at port {}", port);
    }

    private synchronized void onConfigUpdated(CoreConfig prev, CoreConfig curr) {
        if (this.closed) {
            return;
        }

        if (prev.port() != curr.port()) {
            CompletableFuture.delayedExecutor(250, TimeUnit.MILLISECONDS).execute(() -> {
                int existingPort = this.server != null ? this.server.getAddress().getPort() : -1;
                if (existingPort != curr.port()) {
                    if (server != null) {
                        server.stop(1);
                    }
                    start();
                }
            });
        }
    }

    private synchronized void onClose() {
        this.closed = true;

        if (this.server != null) {
            this.server.stop(1);
            this.server = null;
        }

        this.executor.shutdownNow();
    }

    private static boolean isAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException _) {
            // port is occupied
            return false;
        }
    }
}