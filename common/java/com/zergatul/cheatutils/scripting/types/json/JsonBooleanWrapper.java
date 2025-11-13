package com.zergatul.cheatutils.scripting.types.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.zergatul.scripting.Getter;
import com.zergatul.scripting.type.CustomType;

@CustomType(name = "JsonBoolean")
public class JsonBooleanWrapper extends JsonElementWrapper {

    private final boolean value;

    public JsonBooleanWrapper(boolean value) {
        this.value = value;
    }

    @Getter(name = "value")
    public boolean getValue() {
        return value;
    }

    @Override
    protected JsonElement unwrap() {
        return new JsonPrimitive(value);
    }
}