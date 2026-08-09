package com.zergatul.cheatutils.webui;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class WebHelper {

    private WebHelper() {}

    public static void sendException(HttpExchange exchange, int code, Throwable throwable) throws IOException {
        StringWriter writer = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(writer)) {
            throwable.printStackTrace(printWriter);
        }
        sendText(exchange, code, writer.toString());
    }

    public static void sendJson(HttpExchange exchange, int code, String response) throws IOException {
        HttpHelper.setJsonContentType(exchange);
        send(exchange, code, response == null ? "null" : response);
    }

    public static void sendText(HttpExchange exchange, int code, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        send(exchange, code, response == null ? "" : response);
    }

    private static void send(HttpExchange exchange, int code, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream stream = exchange.getResponseBody()) {
            stream.write(bytes);
        } finally {
            exchange.close();
        }
    }
}