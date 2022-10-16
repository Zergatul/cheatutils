package com.zergatul.cheatutils.webui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zergatul.cheatutils.configs.*;
import com.zergatul.cheatutils.utils.MathUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.http.MethodNotSupportedException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ApiHandler implements HttpHandler {

    private final List<ApiBase> apis = new ArrayList<>();

    public ApiHandler() {
        apis.add(new UserApi());
        apis.add(new BlocksConfigApi());
        apis.add(new BlockInfoApi());
        apis.add(new EntityInfoApi());
        apis.add(new EntitiesConfigApi());
        apis.add(new BlockColorApi());
        apis.add(new ScriptsApi());
        apis.add(new ScriptsAssignApi());
        apis.add(new ScriptsDocsApi());

        apis.add(new SimpleConfigApi<FullBrightConfig>("full-bright", FullBrightConfig.class) {
            @Override
            protected FullBrightConfig getConfig() {
                return ConfigStore.instance.getConfig().fullBrightConfig;
            }

            @Override
            protected void setConfig(FullBrightConfig config) {
                ConfigStore.instance.getConfig().fullBrightConfig = config;
            }
        });

        apis.add(new SimpleConfigApi<FreeCamConfig>("free-cam", FreeCamConfig.class) {
            @Override
            protected FreeCamConfig getConfig() {
                return ConfigStore.instance.getConfig().freeCamConfig;
            }

            @Override
            protected void setConfig(FreeCamConfig config) {
                config.acceleration = MathUtils.clamp(config.acceleration, 5, 500);
                config.maxSpeed = MathUtils.clamp(config.maxSpeed, 5, 500);
                config.slowdownFactor = MathUtils.clamp(config.slowdownFactor, 1e-9, 0.5);
                ConfigStore.instance.getConfig().freeCamConfig = config;
            }
        });
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String[] parts = exchange.getRequestURI().getPath().split("/");

        Optional<ApiBase> api;
        synchronized (apis) {
            api = apis.stream().filter(a -> a.getRoute().equals(parts[2])).findFirst();
        }

        if (!api.isPresent()) {
            exchange.sendResponseHeaders(404, 0);
            exchange.close();
            return;
        }

        try {
            switch (exchange.getRequestMethod()) {
                case "GET":
                    processGet(parts, api.get(), exchange);
                    break;
                case "POST":
                    processPost(api.get(), exchange);
                    break;
                case "PUT":
                    processPut(parts, api.get(), exchange);
                    break;
                case "DELETE":
                    processDelete(parts, api.get(), exchange);
                    break;
            }
        }
        catch (MethodNotSupportedException e) {
            exchange.sendResponseHeaders(404, 0);
            exchange.close();
        }
        catch (HttpException e) {
            sendMessage(exchange, 503, e.getMessage());
        }
        catch (Exception e) {
            sendMessage(exchange, 500, e.getMessage());
        }
    }

    private void processGet(String[] parts, ApiBase api, HttpExchange exchange) throws HttpException, IOException {
        String response;
        if (parts.length == 3) {
            response = api.get();
        } else {
            response = api.get(parts[3]);
        }
        byte[] data = response.getBytes(StandardCharsets.UTF_8);
        HttpHelper.setJsonContentType(exchange);
        exchange.sendResponseHeaders(200, data.length);
        OutputStream stream = exchange.getResponseBody();
        stream.write(data);
        stream.close();
        exchange.close();
    }

    private void processPost(ApiBase api, HttpExchange exchange) throws HttpException, IOException {

        String body = IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);
        String response = api.post(body);

        byte[] data = response.getBytes(StandardCharsets.UTF_8);
        HttpHelper.setJsonContentType(exchange);
        exchange.sendResponseHeaders(200, data.length);
        OutputStream stream = exchange.getResponseBody();
        stream.write(data);
        stream.close();
        exchange.close();
    }

    private void processPut(String[] parts, ApiBase api, HttpExchange exchange) throws HttpException, IOException {

        if (parts.length < 4) {
            throw new MethodNotSupportedException("PUT requires id");
        }

        String body = IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);
        api.put(parts[3], body);

        byte[] data = "{}".getBytes(StandardCharsets.UTF_8);
        HttpHelper.setJsonContentType(exchange);
        exchange.sendResponseHeaders(200, data.length);
        OutputStream stream = exchange.getResponseBody();
        stream.write(data);
        stream.close();
        exchange.close();

    }

    private void processDelete(String[] parts, ApiBase api, HttpExchange exchange) throws HttpException, IOException {

        if (parts.length < 4) {
            throw new MethodNotSupportedException("DELETE requires id");
        }

        String response = api.delete(parts[3]);
        byte[] data = response.getBytes(StandardCharsets.UTF_8);
        HttpHelper.setJsonContentType(exchange);
        exchange.sendResponseHeaders(200, data.length);
        OutputStream stream = exchange.getResponseBody();
        stream.write(data);
        stream.close();
        exchange.close();

    }

    private void sendMessage(HttpExchange exchange, int code, String message) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream stream = exchange.getResponseBody();
        stream.write(bytes);
        stream.close();
        exchange.close();
    }
}
