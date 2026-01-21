package com.zergatul.cheatutils.webui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zergatul.cheatutils.utils.ResourceHelper;

import java.io.*;

public class LargeLanguageModelGuideHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        byte[] bytes;
        InputStream stream = ResourceHelper.get("llm/cheatutils-llm-guide.md");
        try (stream) {
            if (stream == null) {
                exchange.sendResponseHeaders(HttpResponseCodes.NOT_FOUND, 0);
                exchange.close();
                return;
            }

            bytes = org.apache.commons.io.IOUtils.toByteArray(stream);
        }

        exchange.getResponseHeaders().set("Content-Type", "text/markdown; charset=UTF-8");

        exchange.sendResponseHeaders(HttpResponseCodes.OK, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
        exchange.close();
    }
}