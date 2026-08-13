package com.zergatul.cheatutils.scripting.monaco;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zergatul.cheatutils.scripting.monaco.adapters.ClassTypeAdapterFactory;
import com.zergatul.cheatutils.scripting.monaco.adapters.TextRangeTypeAdapterFactory;
import com.zergatul.cheatutils.scripting.monaco.adapters.TokenTypeAdapter;
import com.zergatul.scripting.lexer.TokenType;

import java.lang.reflect.Type;

public class MonacoJson {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(new TextRangeTypeAdapterFactory())
            .registerTypeAdapterFactory(new ClassTypeAdapterFactory())
            .registerTypeAdapter(TokenType.class, new TokenTypeAdapter())
            .create();

    private MonacoJson() {}

    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    public static String toJson(Object object, Type type) {
        return GSON.toJson(object, type);
    }
}