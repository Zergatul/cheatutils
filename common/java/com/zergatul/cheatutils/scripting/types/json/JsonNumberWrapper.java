package com.zergatul.cheatutils.scripting.types.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.zergatul.scripting.Getter;
import com.zergatul.scripting.type.CustomType;

@CustomType(name = "JsonNumber")
public class JsonNumberWrapper extends JsonElementWrapper {

    private final double value;

    public JsonNumberWrapper(double value) {
        this.value = value;
    }

    @Getter(name = "value")
    public double getValue() {
        return value;
    }

    @Override
    protected JsonElement unwrap() {
        if (Math.rint(value) == value && Integer.MIN_VALUE <= value && value <= Integer.MAX_VALUE) {
            return new JsonPrimitive((int) value);
        } else {
            return new JsonPrimitive(value);
        }
    }
}