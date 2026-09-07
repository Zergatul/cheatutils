package com.zergatul.cheatutils.webui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zergatul.cheatutils.utils.ResourceHelper;
import org.apache.commons.io.IOUtils;

import java.io.*;

public class StaticFilesHandler implements HttpHandler {
    protected InputStream open(String path) throws IOException {
        return ResourceHelper.get("web/" + (path.equals("/") ? "index.html" : path.substring(1)));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET") && !exchange.getRequestMethod().equals("HEAD")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String path = exchange.getRequestURI().getPath();
            try (InputStream stream = open(path)) {
                if (stream == null) {
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }
                byte[] bytes = IOUtils.toByteArray(stream);
                HttpHelper.setContentType(exchange, path.equals("/") ? "index.html" : path);
                if (exchange.getRequestMethod().equals("HEAD")) {
                    exchange.getResponseHeaders().set("Content-Length", Integer.toString(bytes.length));
                    exchange.sendResponseHeaders(200, -1);
                } else {
                    exchange.sendResponseHeaders(200, bytes.length);
                    exchange.getResponseBody().write(bytes);
                }
            }
        } catch (IOException | RuntimeException e) {
            WebHelper.sendException(exchange, 500, e);
        } finally {
            exchange.close();
        }
    }
}
