package com.zergatul.cheatutils.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ApiHandler implements HttpHandler {

    public ApiHandler() {}

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String[] parts = exchange.getRequestURI().getRawPath().split("/");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = URLDecoder.decode(parts[i], Charset.defaultCharset());
        }

        WebApiBase api = WebApiRegistry.INSTANCE.get(parts[2]);
        if (api == null) {
            byte[] bytes = "API handler not found".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(HttpResponseCodes.NOT_FOUND, bytes.length);
            OutputStream stream = exchange.getResponseBody();
            stream.write(bytes);
            stream.close();
            exchange.close();
            return;
        }

        try {
            switch (exchange.getRequestMethod()) {
                case "GET":
                    processGet(parts, api, exchange);
                    break;
                case "POST":
                    processPost(api, exchange);
                    break;
                case "PUT":
                    processPut(parts, api, exchange);
                    break;
                case "DELETE":
                    processDelete(parts, api, exchange);
                    break;
            }
        } catch (ApiException e) {
            WebHelper.sendException(exchange, e.getCode(), e);
        } catch (Throwable e) {
            WebHelper.sendException(exchange, HttpResponseCodes.INTERNAL_SERVER_ERROR, e);
        }
    }

    private void processGet(String[] parts, WebApiBase api, HttpExchange exchange) throws Throwable {
        String response;
        if (parts.length == 3) {
            response = api.get();
        } else {
            response = api.get(parts[3]);
        }
        byte[] data = response.getBytes(StandardCharsets.UTF_8);
        HttpHelper.setJsonContentType(exchange);
        exchange.sendResponseHeaders(HttpResponseCodes.OK, data.length);
        OutputStream stream = exchange.getResponseBody();
        stream.write(data);
        stream.close();
        exchange.close();
    }

    private void processPost(WebApiBase api, HttpExchange exchange) throws Throwable {
        String body = IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);
        String response = api.post(body);

        byte[] data = response.getBytes(StandardCharsets.UTF_8);
        HttpHelper.setJsonContentType(exchange);
        exchange.sendResponseHeaders(HttpResponseCodes.OK, data.length);
        OutputStream stream = exchange.getResponseBody();
        stream.write(data);
        stream.close();
        exchange.close();
    }

    private void processPut(String[] parts, WebApiBase api, HttpExchange exchange) throws Throwable {
        if (parts.length < 4) {
            throw new ApiException("PUT requires id", HttpResponseCodes.BAD_REQUEST);
        }

        String body = IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);
        String response = api.put(parts[3], body);

        byte[] data = response.getBytes(StandardCharsets.UTF_8);
        HttpHelper.setJsonContentType(exchange);
        exchange.sendResponseHeaders(HttpResponseCodes.OK, data.length);
        OutputStream stream = exchange.getResponseBody();
        stream.write(data);
        stream.close();
        exchange.close();
    }

    private void processDelete(String[] parts, WebApiBase api, HttpExchange exchange) throws Throwable {
        if (parts.length < 4) {
            throw new ApiException("DELETE requires id", HttpResponseCodes.BAD_REQUEST);
        }

        String response = api.delete(parts[3]);
        byte[] data = response.getBytes(StandardCharsets.UTF_8);
        HttpHelper.setJsonContentType(exchange);
        exchange.sendResponseHeaders(HttpResponseCodes.OK, data.length);
        OutputStream stream = exchange.getResponseBody();
        stream.write(data);
        stream.close();
        exchange.close();
    }
}