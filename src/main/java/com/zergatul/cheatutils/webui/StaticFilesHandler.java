package com.zergatul.cheatutils.webui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;

public class StaticFilesHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String filename;
        if (exchange.getRequestURI().getPath().equals("/")) {
            filename = "/index.html";
        } else {
            filename = exchange.getRequestURI().getPath();
        }

        byte[] bytes;
        try (InputStream stream = loadFromResource("web" + filename)) {

            if (stream == null) {
                exchange.sendResponseHeaders(404, 0);
                exchange.close();
                return;
            }

            bytes = org.apache.commons.io.IOUtils.toByteArray(stream);

        }
        catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(503, 0);
            exchange.close();
            return;
        }

        HttpHelper.setContentType(exchange, filename);

        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
        exchange.close();

    }

    private static InputStream loadFromResource(String filename) {
        ClassLoader classLoader = StaticFilesHandler.class.getClassLoader();
        return classLoader.getResourceAsStream(filename);
    }

    private static InputStream loadFromFile(String filename) {
        try {
            return new FileInputStream("C:\\Users\\Zergatul\\source\\repos\\cheatutils-1.19\\src\\main\\resources\\" + filename);
        }
        catch (IOException e) {
            return null;
        }
    }

}
