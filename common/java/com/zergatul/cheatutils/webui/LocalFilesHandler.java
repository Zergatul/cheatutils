package com.zergatul.cheatutils.webui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class LocalFilesHandler implements HttpHandler {

    private static final String PREFIX = "/local/";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
                throw new ApiException("Method not allowed", HttpResponseCodes.METHOD_NOT_ALLOWED);
            }

            Path modsDirectory = Minecraft.getInstance().gameDirectory.toPath()
                    .resolve("mods")
                    .toAbsolutePath()
                    .normalize();
            Path path = modsDirectory
                    .resolve(exchange.getRequestURI().getPath().substring(PREFIX.length()))
                    .normalize();
            if (!path.startsWith(modsDirectory) || !Files.isRegularFile(path)) {
                WebHelper.sendText(exchange, HttpResponseCodes.NOT_FOUND, "File not found.");
                return;
            }

            byte[] bytes;
            try (InputStream stream = Files.newInputStream(path)) {
                bytes = org.apache.commons.io.IOUtils.toByteArray(stream);
            }

            HttpHelper.setContentType(exchange, path.toString());
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
            WebHelper.sendException(exchange, HttpResponseCodes.INTERNAL_SERVER_ERROR, e);
        }
    }
}