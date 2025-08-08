package com.zergatul.cheatutils.scripting.types.json;

import com.zergatul.scripting.type.CustomType;

@CustomType(name = "JsonNumber")
public class JsonNumberWrapper extends JsonElementWrapper {

    private final double value;

    public JsonNumberWrapper(double value) {
        this.value = value;
    }
}