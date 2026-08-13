package com.zergatul.cheatutils.webui;

import com.sun.net.httpserver.HttpServer;
import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.scripting.monaco.Integration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ConfigHttpServer {

    public static ConfigHttpServer instance = new ConfigHttpServer();

    private static final int HTTP_THREADS = 4;

    private final Logger logger = LogManager.getLogger(ConfigHttpServer.class);
    private final ExecutorService executor;
    private HttpServer server;
    private boolean closed;

    private ConfigHttpServer() {
        executor = Executors.newFixedThreadPool(HTTP_THREADS, new HttpThreadFactory());
        Events.Close.add(this::close);
    }

    public synchronized void start() {
        if (closed || server != null) {
            return;
        }

        int port = 0;
        for (int i = 5005; i < 5100; i++) {
            if (isAvailable(i)) {
                port = i;
                break;
            }
        }

        if (port == 0) {
            logger.error("Cannot find a free port for the HTTP server.");
            return;
        }

        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            logger.error("Cannot start the HTTP server on port {}.", port, e);
            return;
        }

        new Integration().attach(server, "/api/code/");
        server.createContext("/api/", new ApiHandler());
        server.createContext("/assets/", new AssetsHandler());
        server.createContext("/", new StaticFilesHandler());

        server.setExecutor(executor);
        server.start();

        logger.info("HTTP server started at port {}.", port);
    }

    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;

        if (server != null) {
            server.stop(1);
            server = null;
        }

        executor.shutdownNow();
    }

    public static boolean isAvailable(int port) {
        try (ServerSocket socket = new ServerSocket()) {
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(port));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static class HttpThreadFactory implements ThreadFactory {

        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, Constants.MOD_NAME + " HTTP-" + counter.incrementAndGet());
        }
    }
}