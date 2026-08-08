package com.zergatul.cheatutils.webui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zergatul.cheatutils.wrappers.ModEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class StaticFilesHandler implements HttpHandler {

    public static final String WEB_DIRECTORY_PROPERTY = "cheatutils.web.dir";

    private static final Logger LOGGER = LogManager.getLogger(StaticFilesHandler.class);

    private final Path fileSystemRoot;

    public StaticFilesHandler() {
        this(getFileSystemRoot());
    }

    StaticFilesHandler(Path fileSystemRoot) {
        this.fileSystemRoot = fileSystemRoot == null ? null : fileSystemRoot.toAbsolutePath().normalize();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
                throw new ApiException("Method not allowed", HttpResponseCodes.METHOD_NOT_ALLOWED);
            }

            String filename = resolveFilename(exchange.getRequestURI().getPath());
            byte[] bytes;
            try (InputStream stream = open(filename)) {
                if (stream == null) {
                    WebHelper.sendText(exchange, HttpResponseCodes.NOT_FOUND, "File not found.");
                    return;
                }
                bytes = org.apache.commons.io.IOUtils.toByteArray(stream);
            }

            HttpHelper.setContentType(exchange, filename);
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
            LOGGER.error("Cannot serve static web file.", e);
            WebHelper.sendException(exchange, HttpResponseCodes.INTERNAL_SERVER_ERROR, e);
        }
    }

    private InputStream open(String filename) throws IOException, ApiException {
        if (fileSystemRoot == null) {
            return loadFromResource("web/" + filename);
        }

        Path realRoot = fileSystemRoot.toRealPath();
        Path path = realRoot.resolve(filename).normalize();
        if (!path.startsWith(realRoot)) {
            throw new ApiException("Invalid static file path", HttpResponseCodes.BAD_REQUEST);
        }
        if (!Files.isRegularFile(path)) {
            return null;
        }
        Path realPath = path.toRealPath();
        if (!realPath.startsWith(realRoot)) {
            throw new ApiException("Invalid static file path", HttpResponseCodes.BAD_REQUEST);
        }
        return Files.newInputStream(realPath);
    }

    private static InputStream loadFromResource(String filename) {
        ClassLoader classLoader = StaticFilesHandler.class.getClassLoader();
        return classLoader.getResourceAsStream(filename);
    }

    private static String resolveFilename(String path) throws ApiException {
        if (path == null || !path.startsWith("/") || path.indexOf('\\') >= 0) {
            throw new ApiException("Invalid static file path", HttpResponseCodes.BAD_REQUEST);
        }

        String filename = path.equals("/") ? "index.html" : path.substring(1);
        String[] segments = filename.split("/", -1);
        for (String segment : segments) {
            if (segment.isEmpty() || segment.equals(".") || segment.equals("..")) {
                throw new ApiException("Invalid static file path", HttpResponseCodes.BAD_REQUEST);
            }
        }
        return filename;
    }

    private static Path getFileSystemRoot() {
        String configured = System.getProperty(WEB_DIRECTORY_PROPERTY);
        if (configured != null && !configured.isBlank()) {
            return Path.of(configured);
        }
        if (ModEnvironment.isProduction) {
            return null;
        }
        return Path.of(System.getProperty("user.dir"), "../../common/resources/web");
    }
}