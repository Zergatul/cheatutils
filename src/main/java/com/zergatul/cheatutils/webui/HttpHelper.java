package com.zergatul.cheatutils.webui;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.io.FilenameUtils;

import java.util.Locale;

public class HttpHelper {

    public static void setContentType(HttpExchange exchange, String filename) {
        String extension = FilenameUtils.getExtension(filename).toLowerCase(Locale.ROOT);
        Headers headers = exchange.getResponseHeaders();
        switch (extension) {
            case "png": headers.add("Content-Type", "image/png"); break;
            case "json": headers.add("Content-Type", "application/json"); break;
            case "html": headers.add("Content-Type", "text/html"); break;
            case "js": headers.add("Content-Type", "text/javascript"); break;
            case "css": headers.add("Content-Type", "text/css"); break;
        }
    }

    public static void setCacheControl(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        headers.add("Cache-Control", "public, max-age=604800");
    }

    public static void setJsonContentType(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        headers.add("Content-Type", "application/json; charset=UTF-8");
    }
}
