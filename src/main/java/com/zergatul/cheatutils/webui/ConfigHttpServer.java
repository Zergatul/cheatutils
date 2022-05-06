package com.zergatul.cheatutils.webui;

import com.sun.net.httpserver.HttpServer;
import com.zergatul.cheatutils.ModMain;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class ConfigHttpServer {

    public static ConfigHttpServer instance = new ConfigHttpServer();

    private HttpServer server;
    private String uri;

    private ConfigHttpServer() {

    }

    public void start() {
        int port = 0;
        for (int i = 5005; i < 5100; i++) {
            if (isAvailable(i)) {
                port = i;
                break;
            }
        }

        if (port == 0) {
            ModMain.LOGGER.debug("Cannot start http server");
            return;
        }

        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }

        server.createContext("/api/", new ApiHandler());
        server.createContext("/assets/", new AssetsHandler());
        server.createContext("/", new StaticFilesHandler());

        server.setExecutor(null);
        server.start();

        uri = "http://localhost:" + port + "/";

        ModMain.LOGGER.debug("Http server started");
    }

    public String getUrl() {
        return uri;
    }

    public static boolean isAvailable(int port) {

        ServerSocket socket = null;
        try {
            socket = new ServerSocket(port);
            socket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }
}
