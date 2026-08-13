package com.zergatul.cheatutils.webui;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class WebHelper {

    public static final int MAX_REQUEST_BODY_SIZE = 64 * 1024 * 1024;

    private WebHelper() {}

    public static void sendException(HttpExchange exchange, int code, Throwable throwable) throws IOException {
        StringWriter writer = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(writer)) {
            throwable.printStackTrace(printWriter);
        }
        sendText(exchange, code, writer.toString());
    }

    public static String readBody(HttpExchange exchange) throws IOException, ApiException {
        String contentLength = exchange.getRequestHeaders().getFirst("Content-Length");
        if (contentLength != null) {
            try {
                long length = Long.parseLong(contentLength);
                if (length < 0) {
                    throw new ApiException("Invalid Content-Length header", HttpResponseCodes.BAD_REQUEST);
                }
                if (length > MAX_REQUEST_BODY_SIZE) {
                    throw new ApiException("Request body is too large", HttpResponseCodes.PAYLOAD_TOO_LARGE);
                }
            } catch (NumberFormatException e) {
                throw new ApiException("Invalid Content-Length header", HttpResponseCodes.BAD_REQUEST, e);
            }
        }

        try (InputStream stream = exchange.getRequestBody()) {
            byte[] bytes = stream.readNBytes(MAX_REQUEST_BODY_SIZE + 1);
            if (bytes.length > MAX_REQUEST_BODY_SIZE) {
                throw new ApiException("Request body is too large", HttpResponseCodes.PAYLOAD_TOO_LARGE);
            }
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    public static void requireJsonContentType(HttpExchange exchange) throws ApiException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("application/json")) {
            throw new ApiException("Content-Type must be application/json", HttpResponseCodes.BAD_REQUEST);
        }
    }

    public static <T> T parseJson(Gson gson, String body, Class<T> type) throws ApiException {
        try {
            T result = gson.fromJson(body, type);
            if (result == null) {
                throw new ApiException("JSON body is required", HttpResponseCodes.BAD_REQUEST);
            }
            return result;
        } catch (JsonParseException e) {
            throw new ApiException("Invalid JSON body", HttpResponseCodes.BAD_REQUEST, e);
        }
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