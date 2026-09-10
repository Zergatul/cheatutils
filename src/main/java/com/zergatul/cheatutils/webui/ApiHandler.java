package com.zergatul.cheatutils.webui;

import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.CoreConfig;
import com.zergatul.cheatutils.configs.FreeCamConfig;
import com.zergatul.cheatutils.configs.MonacoEditorConfig;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class ApiHandler implements HttpHandler {

    private final List<ApiBase> apis = new ArrayList<>();

    public ApiHandler() {
        apis.add(new BlocksConfigApi());
        apis.add(new BlocksConfigApi.Add());
        apis.add(new BlockModelApi());
        apis.add(new RescanChunksApi());
        apis.add(new BlockInfoApi());
        apis.add(new ProfilesApi());
        apis.add(new ResetConfigApi());
        apis.add(new KeyBindingScriptsApi());
        apis.add(new ScriptsAssignApi());
        apis.add(new ScriptsDocsApi());

        apis.add(new SimpleConfigApi<FreeCamConfig>("free-cam", FreeCamConfig.class) {
            @Override
            protected FreeCamConfig getConfig() {
                return ConfigStore.instance.getConfig().freeCamConfig;
            }

            @Override
            protected void setConfig(FreeCamConfig config) {
                ConfigStore.instance.getConfig().freeCamConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<CoreConfig>("core", CoreConfig.class) {
            @Override
            protected CoreConfig getConfig() {
                return ConfigStore.instance.getConfig().coreConfig;
            }

            @Override
            protected void setConfig(CoreConfig config) {
                ConfigStore.instance.getConfig().coreConfig = config;
                ConfigHttpServer.instance.onConfigUpdated();
            }
        });

        apis.add(new SimpleConfigApi<MonacoEditorConfig>("monaco-editor-settings", MonacoEditorConfig.class) {
            @Override
            protected MonacoEditorConfig getConfig() {
                return ConfigStore.instance.getConfig().monacoEditor;
            }

            @Override
            protected void setConfig(MonacoEditorConfig config) {
                ConfigStore.instance.getConfig().monacoEditor = config;
            }
        });
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        FutureTask<String> task = null;
        try {
            String[] parts = exchange.getRequestURI().getRawPath().split("/", -1);
            if (parts.length < 3 || parts.length > 4 || parts[2].isEmpty()) {
                throw new ApiException("API handler not found", 404);
            }
            String route = URLDecoder.decode(parts[2], "UTF-8");
            String id = parts.length == 4 ? URLDecoder.decode(parts[3], "UTF-8") : null;
            ApiBase api = apis.stream().filter(a -> a.getRoute().equals(route)).findFirst().orElse(null);
            if (api == null) {
                throw new ApiException("API handler not found", 404);
            }
            String method = exchange.getRequestMethod();
            String body = IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);
            task = new FutureTask<>(() -> {
                if (ConfigHttpServer.instance.isClosed()) {
                    throw new ApiException("Client is shutting down.", 503);
                }
                try {
                    switch (method) {
                        case "GET": return id == null ? api.get() : api.get(id);
                        case "POST":
                            if (id != null) throw new ApiException("Unexpected id", 400);
                            return api.post(body);
                        case "PUT":
                            if (id == null) throw new ApiException("PUT requires id", 400);
                            return api.put(id, body);
                        case "DELETE":
                            if (id == null) throw new ApiException("DELETE requires id", 400);
                            return api.delete(id);
                        default: throw new ApiException("Method not allowed", 405);
                    }
                } catch (Exception | Error e) {
                    throw e;
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
            Minecraft.getMinecraft().addScheduledTask(task);
            byte[] bytes = task.get(10, TimeUnit.SECONDS).getBytes(StandardCharsets.UTF_8);
            HttpHelper.setJsonContentType(exchange);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
        } catch (Exception e) {
            if (task != null && !task.isDone()) task.cancel(false);
            Throwable cause = e instanceof ExecutionException ? e.getCause() : e;
            int code = cause instanceof ApiException ? ((ApiException) cause).getCode()
                    : cause instanceof JsonParseException || cause instanceof IllegalArgumentException ? 400
                    : cause instanceof TimeoutException || cause instanceof InterruptedException ? 503 : 500;
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            WebHelper.sendException(exchange, code, cause);
        } finally {
            if (task != null && !task.isDone()) task.cancel(false);
            exchange.close();
        }
    }
}