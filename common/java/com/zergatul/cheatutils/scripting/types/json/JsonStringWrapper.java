package com.zergatul.cheatutils.scripting.types.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.zergatul.scripting.Getter;
import com.zergatul.scripting.type.CustomType;

@CustomType(name = "JsonString")
public class JsonStringWrapper extends JsonElementWrapper {

    private final String value;

    public JsonStringWrapper(String value) {
        this.value = value;
    }

    @Getter(name = "value")
    public String getValue() {
        return value;
    }

    @Override
    protected JsonElement unwrap() {
        return new JsonPrimitive(value);
    }
}