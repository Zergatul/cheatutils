package com.zergatul.cheatutils.webui;

import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpApiSmokeTest {

    private static final Logger LOGGER = LogManager.getLogger(HttpApiSmokeTest.class);

    private HttpApiSmokeTest() {}

    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/api/", new ApiHandler(List.of(new SmokeApi())));
        server.setExecutor(executor);
        server.start();

        try {
            URI baseUri = URI.create("http://localhost:" + server.getAddress().getPort() + "/api/");
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

            HttpResponse<String> get = send(client, baseUri.resolve("smoke"), "GET", null);
            require(get, HttpResponseCodes.OK, "{\"ok\":true}");
            if (!get.headers().firstValue("Content-Type").orElse("").startsWith("application/json")) {
                throw new IllegalStateException("Successful API response is not JSON.");
            }
            if (!"nosniff".equals(get.headers().firstValue("X-Content-Type-Options").orElse(null))) {
                throw new IllegalStateException("API response is missing nosniff protection.");
            }

            require(send(client, baseUri.resolve("smoke/value"), "PUT", "payload"),
                    HttpResponseCodes.OK,
                    "{\"id\":\"value\",\"body\":\"payload\"}");
            requireError(send(client, baseUri.resolve("missing"), "GET", null),
                    HttpResponseCodes.NOT_FOUND,
                    "API handler not found");
            requireError(send(client, baseUri.resolve("smoke"), "PATCH", null),
                    HttpResponseCodes.METHOD_NOT_ALLOWED,
                    "Method not allowed");
            requireError(send(client, baseUri.resolve("smoke/value"), "POST", "payload"),
                    HttpResponseCodes.BAD_REQUEST,
                    "POST does not accept id");
            requireError(send(client, baseUri.resolve("smoke"), "PUT", "payload"),
                    HttpResponseCodes.BAD_REQUEST,
                    "PUT requires id");
            requireError(send(client, baseUri.resolve("smoke/failure"), "GET", null),
                    HttpResponseCodes.INTERNAL_SERVER_ERROR,
                    "Smoke API failure");

            LOGGER.info("HTTP API routing and response smoke test passed.");
        } finally {
            server.stop(0);
            executor.shutdownNow();
        }
    }

    private static HttpResponse<String> send(HttpClient client, URI uri, String method, String body) throws Exception {
        HttpRequest.BodyPublisher publisher = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(5))
                .method(method, publisher)
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static void require(HttpResponse<String> response, int status, String body) {
        if (response.statusCode() != status) {
            throw new IllegalStateException("Expected HTTP " + status + " but received " + response.statusCode() + ".");
        }
        if (!body.equals(response.body())) {
            throw new IllegalStateException("Unexpected HTTP response body: " + response.body());
        }
    }

    private static void requireError(HttpResponse<String> response, int status, String message) {
        if (response.statusCode() != status) {
            throw new IllegalStateException("Expected HTTP " + status + " but received " + response.statusCode() + ".");
        }
        if (!response.body().contains(message) || !response.body().contains("\tat ")) {
            throw new IllegalStateException("Error response does not contain the message and stack trace: " + response.body());
        }
    }

    private static class SmokeApi extends ApiBase {

        @Override
        public String getRoute() {
            return "smoke";
        }

        @Override
        public String get() {
            return "{\"ok\":true}";
        }

        @Override
        public String get(String id) {
            throw new IllegalStateException("Smoke API failure");
        }

        @Override
        public String put(String id, String body) {
            return "{\"id\":" + gson.toJson(id) + ",\"body\":" + gson.toJson(body) + "}";
        }
    }
}
