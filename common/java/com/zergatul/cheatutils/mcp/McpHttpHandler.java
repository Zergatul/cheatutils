package com.zergatul.cheatutils.mcp;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zergatul.cheatutils.mcp.protocol.*;
import com.zergatul.cheatutils.mcp.resource.*;
import com.zergatul.cheatutils.mcp.tool.*;
import com.zergatul.cheatutils.webui.HttpResponseCodes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class McpHttpHandler implements HttpHandler {

    private final Logger logger = LogManager.getLogger(McpHttpHandler.class);
    private final Gson gson = new Gson();
    private final McpTool[] tools;
    private final McpResource[] resources;
    private final McpResourceTemplate[] resourceTemplates;

    public McpHttpHandler() {
        this.tools = new McpTool[] {
                new ListScriptTypesTool(),
                new ListScriptExamplesTool(),
                new ListScriptsTool(),
                new GetScriptTool(),
                new CompileScriptTool(),
                new SaveScriptTool(),
                new GetLastAttemptedScriptTool(),
        };
        this.resources = new McpResource[] {
                new LanguageDocumentationResource(),
                new AgentsGuideResource(),
                new ScriptingApiResource(),
        };
        this.resourceTemplates = new McpResourceTemplate[] {
                new ScriptTypeDocumentationResource(),
                new ScriptExampleResource(),
        };
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        boolean isValidPath =
                exchange.getRequestURI().getPath().equals("/mcp") ||
                exchange.getRequestURI().getPath().equals("/mcp/");
        if (!isValidPath) {
            exchange.sendResponseHeaders(HttpResponseCodes.NOT_FOUND, 0);
            exchange.close();
            return;
        }

        if (!exchange.getRequestMethod().equals("POST")) {
            exchange.sendResponseHeaders(HttpResponseCodes.METHOD_NOT_ALLOWED, 0);
            exchange.close();
            return;
        }

        handlePost(exchange);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.startsWith("application/json")) {
            sendBadRequest(exchange, "Bad Content-Type");
            return;
        }

        JsonElement inputRaw;
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody())) {
            inputRaw = JsonParser.parseReader(reader);
        } catch (JsonParseException e) {
            sendBadRequest(exchange, "Cannot parse JSON");
            return;
        }

        if (!isValidJsonRpc(inputRaw)) {
            sendBadRequest(exchange, "Request body is not JSON RPC");
            return;
        }

        JsonObject input = inputRaw.getAsJsonObject();
        if (isNotification(input)) {
            handleMcpNotification(exchange, input);
            return;
        }

        RpcRequest request = parseRpcRequest(input);
        handleMcpRequest(exchange, request);
    }

    private void handleMcpNotification(HttpExchange exchange, JsonObject input) throws IOException {
        String method = input.getAsJsonPrimitive("method").getAsString();
        if (!method.equals("notifications/initialized")) {
            logger.warn("Unsupported notification: {}", method);
        }
        exchange.sendResponseHeaders(HttpResponseCodes.ACCEPTED, 0);
        exchange.close();
    }

    private void handleMcpRequest(HttpExchange exchange, RpcRequest request) throws IOException {
        switch (request.method) {
            case "initialize":
                handleInitializeMethod(exchange, request);
                break;

            case "tools/list":
                handleToolsListMethod(exchange, request);
                break;

            case "tools/call":
                handleToolsCallMethod(exchange, request);
                break;

            case "resources/list":
                handleResourcesListMethod(exchange, request);
                break;

            case "resources/read":
                handleResourceReadMethod(exchange, request);
                break;

            case "resources/templates/list":
                handleResourceTemplatesListMethod(exchange, request);
                break;

            default:
                sendJsonRpcError(exchange, request.id, -32601, "Method not found");
                break;
        }
    }

    private void handleInitializeMethod(HttpExchange exchange, RpcRequest rpcRequest) throws IOException {
        //InitializeRequest request = gson.fromJson(rpcRequest.parameters, InitializeRequest.class);

        sendJsonRpcResult(exchange, rpcRequest.id, new InitializeResult(
                Constants.VERSION,
                new Implementation(com.zergatul.cheatutils.Constants.MOD_NAME + " MCP Server", "1.0"),
                new ServerCapabilities(
                        new ServerCapabilities.Resources(false, false),
                        new ServerCapabilities.Tools(false))));
    }

    private void handleToolsListMethod(HttpExchange exchange, RpcRequest rpcRequest) throws IOException {
        Tool[] tools = Arrays.stream(this.tools).map(t -> new Tool(
                t.getName(),
                t.getTitle(),
                t.getDescription(),
                t.getInputSchema(),
                t.getOutputSchema())).toArray(Tool[]::new);

        sendJsonRpcResult(exchange, rpcRequest.id, new ListToolsResult(tools));
    }

    private void handleToolsCallMethod(HttpExchange exchange, RpcRequest rpcRequest) throws IOException {
        CallToolRequest request = gson.fromJson(rpcRequest.parameters, CallToolRequest.class);

        McpTool tool = Arrays.stream(tools)
                .filter(t -> t.getName().equals(request.name()))
                .findFirst()
                .orElse(null);

        if (tool == null) {
            sendJsonRpcError(exchange, rpcRequest.id, -32002, "Resource not found");
            return;
        }

        JsonObject result = tool.invoke(request.arguments());
        String resultJson = new String(serializeJson(result), StandardCharsets.UTF_8);
        sendJsonRpcResult(exchange, rpcRequest.id, new CallToolResult(
                new ContentBlock[] { new TextContent(resultJson) },
                result,
                null));
    }

    private void handleResourcesListMethod(HttpExchange exchange, RpcRequest rpcRequest) throws IOException {
        Resource[] resources = Arrays.stream(this.resources).map(r -> new Resource(
                r.getUri(),
                r.getName(),
                r.getTitle(),
                r.getDescription(),
                r.getMimeType())).toArray(Resource[]::new);

        sendJsonRpcResult(exchange, rpcRequest.id, new ListResourcesResult(resources));
    }

    private void handleResourceReadMethod(HttpExchange exchange, RpcRequest rpcRequest) throws IOException {
        ReadResourceRequest request = gson.fromJson(rpcRequest.parameters, ReadResourceRequest.class);

        McpResource resource = Arrays.stream(resources)
                .filter(r -> r.getUri().equals(request.uri()))
                .findFirst()
                .orElse(null);

        if (resource != null) {
            ResourceContents content = resource.getContent();
            sendJsonRpcResult(exchange, rpcRequest.id, ReadResourceResult.of(content));
            return;
        }

        McpResourceTemplate template = Arrays.stream(resourceTemplates)
                .filter(t -> t.hasResource(request.uri()))
                .findFirst()
                .orElse(null);

        if (template != null) {
            ResourceContents content = template.getContent(request.uri());
            sendJsonRpcResult(exchange, rpcRequest.id, ReadResourceResult.of(content));
            return;
        }

        sendJsonRpcError(exchange, rpcRequest.id, -32002, "Resource not found");
    }

    private void handleResourceTemplatesListMethod(HttpExchange exchange, RpcRequest rpcRequest) throws IOException {
        ResourceTemplate[] templates = Arrays.stream(this.resourceTemplates).map(r -> new ResourceTemplate(
                r.getUriTemplate().getTemplate(),
                r.getName(),
                r.getTitle(),
                r.getDescription(),
                r.getMimeType())).toArray(ResourceTemplate[]::new);

        sendJsonRpcResult(exchange, rpcRequest.id, new ListResourceTemplatesResult(templates));
    }

    private RpcRequest parseRpcRequest(JsonObject request) {
        RequestId id = parseRequestId(request.get("id").getAsJsonPrimitive());
        String method = request.get("method").getAsString();
        JsonObject parameters = request.getAsJsonObject("params");
        return new RpcRequest(id, method, parameters);
    }

    private RequestId parseRequestId(JsonPrimitive element) {
        if (element.isNumber()) {
            return new RequestId(element.getAsInt());
        }
        if (element.isString()) {
            return new RequestId(element.getAsString());
        }
        throw new IllegalStateException();
    }

    private void sendJsonRpcResult(HttpExchange exchange, RequestId id, Object result) throws IOException {
        JsonObject response = createBaseJsonRpcResponse(id);
        response.add("result", gson.toJsonTree(result));

        sendJson(exchange, response);
    }

    private void sendJsonRpcError(HttpExchange exchange, RequestId id, int code, String message) throws IOException {
        JsonObject response = createBaseJsonRpcResponse(id);

        JsonObject error = new JsonObject();
        error.addProperty("code", code);
        error.addProperty("message", message);

        response.add("error", error);

        sendJson(exchange, response);
    }

    private void sendJsonRpcError(HttpExchange exchange, int code, String message) throws IOException {
        JsonObject response = createBaseJsonRpcResponse();

        JsonObject error = new JsonObject();
        error.addProperty("code", code);
        error.addProperty("message", message);

        response.add("error", error);

        sendJson(exchange, response);
    }

    private JsonObject createBaseJsonRpcResponse(RequestId id) {
        JsonObject object = createBaseJsonRpcResponse();
        if (id.numValue() != null) {
            object.addProperty("id", id.numValue());
        } else {
            object.addProperty("id", id.strValue());
        }
        return object;
    }

    private JsonObject createBaseJsonRpcResponse() {
        JsonObject object = new JsonObject();
        object.addProperty("jsonrpc", "2.0");
        return object;
    }

    private boolean isValidJsonRpc(JsonElement element) {
        if (!element.isJsonObject()) {
            return false;
        }

        JsonObject object = element.getAsJsonObject();

        JsonElement jsonRpcElement = object.get("jsonrpc");
        if (jsonRpcElement == null) {
            return false;
        }
        if (!jsonRpcElement.isJsonPrimitive()) {
            return false;
        }
        if (!jsonRpcElement.getAsJsonPrimitive().isString()) {
            return false;
        }
        if (!jsonRpcElement.getAsString().equals("2.0")) {
            return false;
        }

        JsonElement idElement = object.get("id");
        if (idElement != null) {
            if (!idElement.isJsonPrimitive()) {
                return false;
            }
            if (!idElement.getAsJsonPrimitive().isNumber() && !idElement.getAsJsonPrimitive().isString()) {
                return false;
            }
        }

        JsonElement methodElement = object.get("method");
        if (methodElement == null) {
            return false;
        }
        if (!methodElement.isJsonPrimitive()) {
            return false;
        }
        if (!methodElement.getAsJsonPrimitive().isString()) {
            return false;
        }

        return true;
    }

    private boolean isNotification(JsonObject object) {
        return object.get("id") == null;
    }

    private void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(HttpResponseCodes.BAD_REQUEST, data.length);
        exchange.getResponseBody().write(data);
        exchange.close();
    }

    private void sendJson(HttpExchange exchange, JsonElement element) throws IOException {
        byte[] data = serializeJson(element);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(HttpResponseCodes.OK, data.length);
        exchange.getResponseBody().write(data);
        exchange.close();
    }

    private byte[] serializeJson(JsonElement element) throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            try (OutputStreamWriter byteWriter = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
                try (JsonWriter jsonWriter = new JsonWriter(byteWriter)) {
                    Streams.write(element, jsonWriter);
                    return stream.toByteArray();
                }
            }
        }
    }

    private record RpcRequest(RequestId id, String method, JsonObject parameters) {}
}