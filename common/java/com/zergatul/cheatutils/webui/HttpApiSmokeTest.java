package com.zergatul.cheatutils.webui;

import com.sun.net.httpserver.HttpServer;
import com.zergatul.cheatutils.common.ModLoaderBridge;
import com.zergatul.cheatutils.common.ModLoaderBridgeInstance;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.monaco.Integration;
import com.zergatul.cheatutils.scripting.monaco.MonacoJson;
import net.minecraft.SharedConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class HttpApiSmokeTest {

    private static final Logger LOGGER = LogManager.getLogger(HttpApiSmokeTest.class);

    private HttpApiSmokeTest() {}

    public static void main(String[] args) throws Exception {
        SharedConstants.tryDetectVersion();
        ModLoaderBridgeInstance.init(new SmokeModLoaderBridge());
        verifyClientThreadDispatcher();

        Path webRoot = Files.createTempDirectory("cheatutils-http-web-");
        Files.writeString(webRoot.resolve("index.html"), "smoke-index", StandardCharsets.UTF_8);
        Files.createDirectories(webRoot.resolve("styles"));
        Files.writeString(webRoot.resolve("styles/test.css"), "smoke-style", StandardCharsets.UTF_8);
        Files.write(webRoot.resolve("test.ttf"), new byte[] { 0, 1, 2, 3 });
        String previousWebDirectory = System.getProperty(StaticFilesHandler.WEB_DIRECTORY_PROPERTY);
        System.setProperty(StaticFilesHandler.WEB_DIRECTORY_PROPERTY, webRoot.toString());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        new Integration().attach(server, "/api/code/");
        server.createContext("/api/", new ApiHandler(List.of(
                new SmokeApi(),
                new GeneralInformationApi(),
                new ScriptTypesApi(),
                new ScriptWorkspaceApi(),
                new ScriptCompileApi(),
                new ScriptsDocsApi(),
                new KeyBindingScriptsApi(),
                new ScriptsAssignApi(),
                new StatusOverlayCodeApi(),
                new BlockAutomationCodeApi(),
                new VillagerRollerCodeApi(),
                new EventsScriptingCodeApi(),
                new CoreConfigApi(),
                new SmokeConfigApi(),
                new SmokeValidationApi())));
        server.createContext("/", new StaticFilesHandler());
        server.setExecutor(executor);
        server.start();
        InetSocketAddress serverAddress = server.getAddress();

        try {
            URI siteUri = URI.create("http://localhost:" + serverAddress.getPort() + "/");
            URI baseUri = siteUri.resolve("api/");
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

            verifyStaticFiles(client, siteUri);
            verifyCodeApi(client, baseUri.resolve("code/"));
            verifyWorkspaceApi(client, baseUri);
            verifyCoreApi(client, baseUri);
            verifyConcurrentRequests(client, baseUri);

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
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("HTTP executor did not terminate.");
                }
                try (ServerSocket socket = new ServerSocket()) {
                    socket.setReuseAddress(true);
                    socket.bind(serverAddress);
                }
            } finally {
                if (previousWebDirectory == null) {
                    System.clearProperty(StaticFilesHandler.WEB_DIRECTORY_PROPERTY);
                } else {
                    System.setProperty(StaticFilesHandler.WEB_DIRECTORY_PROPERTY, previousWebDirectory);
                }
                deleteTree(webRoot);
            }
        }
    }

    private static void verifyStaticFiles(HttpClient client, URI siteUri) throws Exception {
        HttpResponse<String> index = send(client, siteUri, "GET", null);
        require(index, HttpResponseCodes.OK, "smoke-index");
        if (!index.headers().firstValue("Content-Type").orElse("").startsWith("text/html")) {
            throw new IllegalStateException("Static index response does not have an HTML content type.");
        }
        if (!"nosniff".equals(index.headers().firstValue("X-Content-Type-Options").orElse(null))) {
            throw new IllegalStateException("Static response is missing nosniff protection.");
        }

        require(send(client, siteUri.resolve("styles/test.css"), "GET", null),
                HttpResponseCodes.OK,
                "smoke-style");
        HttpResponse<String> font = send(client, siteUri.resolve("test.ttf"), "GET", null);
        if (!font.headers().firstValue("Content-Type").orElse("").startsWith("font/ttf")) {
            throw new IllegalStateException("Static font response does not have a TTF content type.");
        }
        requireContains(send(client, siteUri.resolve("missing.js"), "GET", null),
                HttpResponseCodes.NOT_FOUND,
                "File not found");
        requireError(send(client, URI.create(siteUri + "%2e%2e/secret.txt"), "GET", null),
                HttpResponseCodes.BAD_REQUEST,
                "Invalid static file path");
        requireError(send(client, siteUri, "POST", ""),
                HttpResponseCodes.METHOD_NOT_ALLOWED,
                "Method not allowed");
    }

    private static void verifyConcurrentRequests(HttpClient client, URI baseUri) throws Exception {
        List<CompletableFuture<HttpResponse<String>>> requests = IntStream.range(0, 16)
                .mapToObj(index -> client.sendAsync(
                        HttpRequest.newBuilder(baseUri.resolve("smoke"))
                                .timeout(Duration.ofSeconds(5))
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)))
                .toList();
        CompletableFuture.allOf(requests.toArray(CompletableFuture[]::new)).get(10, TimeUnit.SECONDS);
        for (CompletableFuture<HttpResponse<String>> request : requests) {
            require(request.get(), HttpResponseCodes.OK, "{\"ok\":true}");
        }
    }

    private static void verifyCoreApi(HttpClient client, URI baseUri) throws Exception {
        HttpResponse<String> defaults = send(client, baseUri.resolve("core"), "GET", null);
        requireContains(defaults, HttpResponseCodes.OK, "\"port\": 5005");
        requireContains(defaults, HttpResponseCodes.OK, "\"advancedScripting\": false");

        HttpResponse<String> sanitized = send(
                client,
                baseUri.resolve("core"),
                "POST",
                "{\"port\":0,\"advancedScripting\":true}");
        requireContains(sanitized, HttpResponseCodes.OK, "\"port\": 1");
        requireContains(sanitized, HttpResponseCodes.OK, "\"advancedScripting\": true");

        HttpResponse<String> restored = send(
                client,
                baseUri.resolve("core"),
                "POST",
                "{\"port\":5005,\"advancedScripting\":false}");
        requireContains(restored, HttpResponseCodes.OK, "\"port\": 5005");
        requireContains(restored, HttpResponseCodes.OK, "\"advancedScripting\": false");
    }

    private static void deleteTree(Path root) throws Exception {
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private static HttpResponse<String> send(HttpClient client, URI uri, String method, String body) throws Exception {
        return send(client, uri, method, body, true);
    }

    private static HttpResponse<String> send(
            HttpClient client,
            URI uri,
            String method,
            String body,
            boolean jsonContentType
    ) throws Exception {
        HttpRequest.BodyPublisher publisher = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8);
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(5))
                .method(method, publisher);
        if (body != null && jsonContentType) {
            builder.header("Content-Type", "application/json; charset=UTF-8");
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
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

    private static void requireContains(HttpResponse<String> response, int status, String text) {
        if (response.statusCode() != status) {
            throw new IllegalStateException("Expected HTTP " + status + " but received " + response.statusCode() + ".");
        }
        if (!response.body().contains(text)) {
            throw new IllegalStateException("HTTP response does not contain '" + text + "': " + response.body());
        }
    }

    private static void verifyCodeApi(HttpClient client, URI baseUri) throws Exception {
        requireContains(send(client, baseUri.resolve("token-types"), "GET", null),
                HttpResponseCodes.OK,
                "KEYWORD");
        requireContains(send(client, baseUri.resolve("token-modifiers"), "GET", null),
                HttpResponseCodes.OK,
                "PREDEFINED_TYPE");

        for (ScriptType type : ScriptType.values()) {
            require(send(
                            client,
                            baseUri.resolve("diagnostics"),
                            "POST",
                            MonacoJson.toJson(new Integration.CodeRequest("", type.name()))),
                    HttpResponseCodes.OK,
                    "[]");
        }

        String validCode = "int value = 1;";
        requireContains(send(
                        client,
                        baseUri.resolve("tokenize"),
                        "POST",
                        MonacoJson.toJson(new Integration.CodeRequest(validCode, "OVERLAY"))),
                HttpResponseCodes.OK,
                "\"type\"");
        requireContains(send(
                        client,
                        baseUri.resolve("diagnostics"),
                        "POST",
                        MonacoJson.toJson(new Integration.CodeRequest("int value = ;", "OVERLAY"))),
                HttpResponseCodes.OK,
                "\"message\"");
        requireContains(send(
                        client,
                        baseUri.resolve("completion"),
                        "POST",
                        MonacoJson.toJson(new Integration.PositionRequest("main.", "OVERLAY", 1, 6))),
                HttpResponseCodes.OK,
                "addText");
        requireContains(send(
                        client,
                        baseUri.resolve("hover"),
                        "POST",
                        MonacoJson.toJson(new Integration.PositionRequest("main.addText(\"x\");", "OVERLAY", 1, 6))),
                HttpResponseCodes.OK,
                "addText");
        requireContains(send(
                        client,
                        baseUri.resolve("hover"),
                        "POST",
                        MonacoJson.toJson(new Integration.PositionRequest("tps.get();", "OVERLAY", 1, 5))),
                HttpResponseCodes.OK,
                "Estimates server TPS from the last 20 time updates");
        requireContains(send(
                        client,
                        baseUri.resolve("completion"),
                        "POST",
                        MonacoJson.toJson(new Integration.PositionRequest("events.", "EVENTS", 1, 8))),
                HttpResponseCodes.OK,
                "onTickEnd");
        requireContains(send(
                        client,
                        baseUri.resolve("hover"),
                        "POST",
                        MonacoJson.toJson(new Integration.PositionRequest(
                                "events.onTickEnd(() => {});",
                                "EVENTS",
                                1,
                                9))),
                HttpResponseCodes.OK,
                "Runs at the end of each client tick");
        requireContains(send(
                        client,
                        baseUri.resolve("hover"),
                        "POST",
                        MonacoJson.toJson(new Integration.PositionRequest(
                                "player.disconnect(\"\", \"Low HP\");",
                                "EVENTS",
                                1,
                                9))),
                HttpResponseCodes.OK,
                "Allowed disconnect types");
        requireContains(send(
                        client,
                        baseUri.resolve("definition"),
                        "POST",
                        MonacoJson.toJson(new Integration.PositionRequest(
                                "int value = 1;\nvalue = 2;",
                                "OVERLAY",
                                2,
                                1))),
                HttpResponseCodes.OK,
                "\"line1\":1");
        requireContains(send(
                        client,
                        baseUri.resolve("color-strings"),
                        "POST",
                        MonacoJson.toJson("string color = \"#ff0000\";")),
                HttpResponseCodes.OK,
                "\"red\":1.0");

        requireError(send(
                        client,
                        baseUri.resolve("diagnostics"),
                        "POST",
                        MonacoJson.toJson(new Integration.CodeRequest(validCode, "UNKNOWN"))),
                HttpResponseCodes.BAD_REQUEST,
                "Unsupported script type");
        requireError(send(client, baseUri.resolve("diagnostics"), "POST", "{}", false),
                HttpResponseCodes.BAD_REQUEST,
                "Content-Type must be application/json");
        requireError(send(client, baseUri.resolve("diagnostics"), "GET", null),
                HttpResponseCodes.METHOD_NOT_ALLOWED,
                "Method not allowed");
    }

    private static void verifyWorkspaceApi(HttpClient client, URI baseUri) throws Exception {
        requireContains(send(client, baseUri.resolve("general-information"), "GET", null),
                HttpResponseCodes.OK,
                "Fabric: smoke-loader");
        requireContains(send(client, baseUri.resolve("script-types"), "GET", null),
                HttpResponseCodes.OK,
                "\"type\": \"KEYBINDING\"");
        requireContains(send(client, baseUri.resolve("script-types"), "GET", null),
                HttpResponseCodes.OK,
                "\"multiple\": true");
        requireContains(send(client, baseUri.resolve("script-workspace/OVERLAY"), "GET", null),
                HttpResponseCodes.OK,
                "\"type\":\"OVERLAY\"");

        String valid = "{\"type\":\"OVERLAY\",\"code\":\"main.addText(\\\"ok\\\");\"}";
        requireContains(send(client, baseUri.resolve("script-compile"), "POST", valid),
                HttpResponseCodes.OK,
                "\"ok\":true");
        String invalid = "{\"type\":\"OVERLAY\",\"code\":\"int value = ;\"}";
        requireContains(send(client, baseUri.resolve("script-compile"), "POST", invalid),
                HttpResponseCodes.OK,
                "\"ok\":false");
        requireContains(send(client, baseUri.resolve("script-compile"), "POST", invalid),
                HttpResponseCodes.OK,
                "\"range\"");

        requireError(send(
                        client,
                        baseUri.resolve("script-compile"),
                        "POST",
                        "{\"type\":\"UNKNOWN\",\"code\":\"\"}"),
                HttpResponseCodes.BAD_REQUEST,
                "Unsupported script type");
        requireError(send(client, baseUri.resolve("script-compile"), "POST", valid, false),
                HttpResponseCodes.BAD_REQUEST,
                "Content-Type must be application/json");
        requireError(send(client, baseUri.resolve("script-workspace/UNKNOWN"), "GET", null),
                HttpResponseCodes.BAD_REQUEST,
                "Unsupported script type");

        for (ScriptType type : ScriptType.values()) {
            HttpResponse<String> docs = send(client, baseUri.resolve("scripts-doc/" + type.name()), "GET", null);
            requireContains(docs, HttpResponseCodes.OK, "**********");
        }
        requireContains(send(client, baseUri.resolve("scripts-doc/KEYBINDING"), "GET", null),
                HttpResponseCodes.OK,
                "Future");
        requireContains(send(client, baseUri.resolve("scripts-doc/EVENTS"), "GET", null),
                HttpResponseCodes.OK,
                "onTickEnd");
        for (String legacyType : List.of("overlay", "handle-keybindings", "block-placer", "villager-roller")) {
            requireContains(send(client, baseUri.resolve("scripts-doc/" + legacyType), "GET", null),
                    HttpResponseCodes.OK,
                    "main.");
        }
        requireError(send(client, baseUri.resolve("scripts-doc/auto-disconnect"), "GET", null),
                HttpResponseCodes.BAD_REQUEST,
                "Unsupported script type");
        requireError(send(client, baseUri.resolve("scripts-doc/UNKNOWN"), "GET", null),
                HttpResponseCodes.BAD_REQUEST,
                "Unsupported script type");

        require(send(client, baseUri.resolve("keybinding-scripts"), "GET", null),
                HttpResponseCodes.OK,
                "[]");
        require(send(client, baseUri.resolve("keybinding-scripts/missing"), "GET", null),
                HttpResponseCodes.OK,
                "null");
        requireError(send(client, baseUri.resolve("keybinding-scripts"), "POST", "{}", false),
                HttpResponseCodes.BAD_REQUEST,
                "Content-Type must be application/json");
        requireError(send(client, baseUri.resolve("keybinding-scripts"), "POST", "{"),
                HttpResponseCodes.BAD_REQUEST,
                "Invalid JSON body");
        requireError(send(client, baseUri.resolve("keybinding-scripts"), "POST", "{}"),
                HttpResponseCodes.BAD_REQUEST,
                "Field is required: name");
        requireError(send(client, baseUri.resolve("keybinding-scripts-assign/test"), "PUT", "30"),
                HttpResponseCodes.BAD_REQUEST,
                "Key binding index must be between -1 and 29");
        requireError(send(client, baseUri.resolve("scripts"), "GET", null),
                HttpResponseCodes.NOT_FOUND,
                "API handler not found");
        verifyCodeSaveEndpointValidation(client, baseUri.resolve("status-overlay-code"));
        verifyCodeSaveEndpointValidation(client, baseUri.resolve("block-automation-code"));
        verifyCodeSaveEndpointValidation(client, baseUri.resolve("villager-roller-code"));
        verifyCodeSaveEndpointValidation(client, baseUri.resolve("events-scripting-code"));
        requireError(send(client, baseUri.resolve("auto-disconnect-code"), "POST", "\"code\""),
                HttpResponseCodes.NOT_FOUND,
                "API handler not found");
        requireError(send(client, baseUri.resolve("auto-disconnect"), "GET", null),
                HttpResponseCodes.NOT_FOUND,
                "API handler not found");
        requireError(send(client, baseUri.resolve("scripted-block-placer-code"), "POST", "\"code\""),
                HttpResponseCodes.NOT_FOUND,
                "API handler not found");

        requireContains(send(client, baseUri.resolve("smoke-config"), "GET", null),
                HttpResponseCodes.OK,
                "\"value\": 1");
        requireError(send(client, baseUri.resolve("smoke-config"), "POST", "{}", false),
                HttpResponseCodes.BAD_REQUEST,
                "Content-Type must be application/json");
        requireError(send(client, baseUri.resolve("smoke-config"), "POST", "{"),
                HttpResponseCodes.BAD_REQUEST,
                "Invalid JSON body");

        require(send(client,
                        baseUri.resolve("smoke-validation"),
                        "POST",
                        "{\"name\":\"test\",\"value\":1,\"data\":\"b2s=\"}"),
                HttpResponseCodes.OK,
                "{\"bytes\":2}");
        requireError(send(client,
                        baseUri.resolve("smoke-validation"),
                        "POST",
                        "{\"value\":1,\"data\":\"b2s=\"}"),
                HttpResponseCodes.BAD_REQUEST,
                "Field is required: name");
        requireError(send(client,
                        baseUri.resolve("smoke-validation"),
                        "POST",
                        "{\"name\":\" \",\"value\":1,\"data\":\"b2s=\"}"),
                HttpResponseCodes.BAD_REQUEST,
                "Field cannot be blank: name");
        requireError(send(client,
                        baseUri.resolve("smoke-validation"),
                        "POST",
                        "{\"name\":\"test\",\"value\":1e309,\"data\":\"b2s=\"}"),
                HttpResponseCodes.BAD_REQUEST,
                "Field must be finite: value");
        requireError(send(client,
                        baseUri.resolve("smoke-validation"),
                        "POST",
                        "{\"name\":\"test\",\"value\":1,\"data\":\"!\"}"),
                HttpResponseCodes.BAD_REQUEST,
                "Field is not valid Base64: data");
    }

    private static void verifyCodeSaveEndpointValidation(HttpClient client, URI uri) throws Exception {
        requireError(send(client, uri, "POST", "\"code\"", false),
                HttpResponseCodes.BAD_REQUEST,
                "Content-Type must be application/json");
        requireError(send(client, uri, "POST", "{"),
                HttpResponseCodes.BAD_REQUEST,
                "Invalid JSON body");
        requireError(send(client, uri, "POST", "null"),
                HttpResponseCodes.BAD_REQUEST,
                "JSON body is required");
        requireError(send(client, uri, "GET", null),
                HttpResponseCodes.METHOD_NOT_ALLOWED,
                "Method not allowed");
    }

    private static void verifyClientThreadDispatcher() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch release = new CountDownLatch(1);
        try {
            Thread testThread = Thread.currentThread();
            Thread directThread = ClientThreadDispatcher.call(executor, true, Thread::currentThread, 1_000);
            if (directThread != testThread) {
                throw new IllegalStateException("Client-thread dispatcher did not execute an already-on-thread task directly.");
            }

            Thread dispatchedThread = ClientThreadDispatcher.call(executor, false, Thread::currentThread, 1_000);
            if (dispatchedThread == testThread) {
                throw new IllegalStateException("Client-thread dispatcher did not use the supplied executor.");
            }

            try {
                ClientThreadDispatcher.call(executor, false, () -> {
                    throw new IllegalStateException("Dispatcher failure");
                }, 1_000);
                throw new IllegalStateException("Client-thread dispatcher swallowed a task exception.");
            } catch (IllegalStateException e) {
                if (!"Dispatcher failure".equals(e.getMessage())) {
                    throw e;
                }
            }

            ExecutorService rejectedExecutor = Executors.newSingleThreadExecutor();
            rejectedExecutor.shutdownNow();
            try {
                ClientThreadDispatcher.call(rejectedExecutor, false, () -> null, 1_000);
                throw new IllegalStateException("Client-thread dispatcher accepted work after shutdown.");
            } catch (ApiException e) {
                if (e.getCode() != HttpResponseCodes.SERVICE_UNAVAILABLE) {
                    throw e;
                }
            }

            try {
                ClientThreadDispatcher.call(executor, false, () -> {
                    release.await();
                    return null;
                }, 10);
                throw new IllegalStateException("Client-thread dispatcher did not time out.");
            } catch (ApiException e) {
                if (e.getCode() != HttpResponseCodes.GATEWAY_TIMEOUT) {
                    throw e;
                }
            }
        } finally {
            release.countDown();
            executor.shutdownNow();
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

    private static class SmokeConfigApi extends SimpleConfigApi<SmokeConfig> {

        private SmokeConfig config = new SmokeConfig();

        private SmokeConfigApi() {
            super("smoke-config", SmokeConfig.class);
        }

        @Override
        protected SmokeConfig getConfig() {
            return config;
        }

        @Override
        protected void setConfig(SmokeConfig config) {
            this.config = config;
        }
    }

    private static class SmokeConfig {
        public int value = 1;
    }

    private static class SmokeValidationApi extends ApiBase {

        @Override
        public String getRoute() {
            return "smoke-validation";
        }

        @Override
        public boolean requiresJsonContentType() {
            return true;
        }

        @Override
        public String post(String body) throws ApiException {
            SmokeValidationRequest request = WebHelper.parseJson(gson, body, SmokeValidationRequest.class);
            WebHelper.requireNonBlankField(request.name, "name");
            WebHelper.requireFinite(WebHelper.requireField(request.value, "value"), "value");
            byte[] bytes = WebHelper.decodeBase64(request.data, "data");
            return "{\"bytes\":" + bytes.length + "}";
        }
    }

    private record SmokeValidationRequest(String name, Double value, String data) {}

    private static class SmokeModLoaderBridge implements ModLoaderBridge {

        @Override
        public String getModLoaderName() {
            return "Fabric";
        }

        @Override
        public String getModLoaderVersion() {
            return "smoke-loader";
        }

        @Override
        public String getModVersion() {
            return "smoke-mod";
        }

        @Override
        public int getModCount() {
            return 1;
        }
    }
}
