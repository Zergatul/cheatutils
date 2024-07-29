package com.zergatul.cheatutils.scripting.monaco;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.zergatul.cheatutils.scripting.monaco.adapters.ClassTypeAdapterFactory;
import com.zergatul.cheatutils.scripting.monaco.adapters.NodeTypeAdapter;
import com.zergatul.cheatutils.scripting.monaco.adapters.TextRangeTypeAdapterFactory;
import com.zergatul.cheatutils.scripting.monaco.adapters.TokenTypeAdapter;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.parser.NodeType;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class Json {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new TextRangeTypeAdapterFactory())
            .registerTypeAdapterFactory(new ClassTypeAdapterFactory())
            .registerTypeAdapter(TokenType.class, new TokenTypeAdapter())
            .registerTypeAdapter(NodeType.class, new NodeTypeAdapter())
            .create();

    public static byte[] toJson(Object object) {
        String str = gson.toJson(object);
        return str.getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] toJson(Object object, Type type) {
        String str = gson.toJson(object, type);
        return str.getBytes(StandardCharsets.UTF_8);
    }

    public static void sendResponse(HttpExchange exchange, Object object) throws IOException {
        byte[] bytes = toJson(object);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    public static void sendResponse(HttpExchange exchange, Object object, Type type) throws IOException {
        byte[] bytes = toJson(object, type);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
}