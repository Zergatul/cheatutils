package com.zergatul.cheatutils.scripting.types.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.zergatul.scripting.type.CustomType;

@CustomType(name = "JsonElement")
public abstract class JsonElementWrapper {

    public static JsonElementWrapper parse(String json) {
        try {
            return from(JsonParser.parseString(json));
        } catch (JsonParseException e) {
            return JsonInvalidWrapper.INSTANCE;
        }
    }

    protected static JsonElementWrapper from(JsonElement element) {
        if (element.isJsonArray()) {
            return new JsonArrayWrapper(element.getAsJsonArray());
        }
        if (element.isJsonObject()) {
            return new JsonObjectWrapper(element.getAsJsonObject());
        }
        if (element.isJsonNull()) {
            return JsonNullWrapper.INSTANCE;
        }
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return JsonBooleanWrapper.from(primitive.getAsBoolean());
            }
            if (primitive.isNumber()) {
                return new JsonNumberWrapper(primitive.getAsDouble());
            }
            if (primitive.isString()) {
                return new JsonStringWrapper(primitive.getAsString());
            }
        }
        return JsonInvalidWrapper.INSTANCE;
    }
}