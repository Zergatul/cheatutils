package com.zergatul.cheatutils.webui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zergatul.cheatutils.scripting.*;
import com.zergatul.scripting.type.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ApiGenHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            List<String> lines = ApiDocsGenerator.generateLines();

            exchange.getResponseHeaders().add("Content-Type", "text/plain");

            StringBuilder builder = new StringBuilder();
            for (String line : lines) {
                builder.append(line);
                builder.append('\n');
            }
            byte[] raw = builder.toString().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, raw.length);
            exchange.getResponseBody().write(raw);
            exchange.close();
        } catch (Throwable e) {
            WebHelper.sendException(exchange, HttpResponseCodes.INTERNAL_SERVER_ERROR, e);
        }
    }
}