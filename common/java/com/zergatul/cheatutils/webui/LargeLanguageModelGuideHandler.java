package com.zergatul.cheatutils.webui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LargeLanguageModelGuideHandler implements HttpHandler {

    private static final Logger LOGGER = LogManager.getLogger(LargeLanguageModelGuideHandler.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
                throw new ApiException("Method not allowed", HttpResponseCodes.METHOD_NOT_ALLOWED);
            }

            byte[] bytes;
            try (InputStream stream = LargeLanguageModelGuideHandler.class.getClassLoader().getResourceAsStream("llm/cheatutils-llm-guide.md")) {
                if (stream == null) {
                    WebHelper.sendText(exchange, HttpResponseCodes.NOT_FOUND, "File not found.");
                    return;
                }
                bytes = org.apache.commons.io.IOUtils.toByteArray(stream);
            }

            exchange.getResponseHeaders().set("Content-Type", "text/markdown; charset=UTF-8");
            exchange.getResponseHeaders().set("Cache-Control", "no-store");
            exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
            exchange.sendResponseHeaders(HttpResponseCodes.OK, bytes.length);
            try (OutputStream stream = exchange.getResponseBody()) {
                stream.write(bytes);
            } finally {
                exchange.close();
            }
        } catch (ApiException e) {
            WebHelper.sendException(exchange, e.getCode(), e);
        } catch (Throwable e) {
            LOGGER.error("Cannot serve the LLM guide.", e);
            WebHelper.sendException(exchange, HttpResponseCodes.INTERNAL_SERVER_ERROR, e);
        }
    }
}