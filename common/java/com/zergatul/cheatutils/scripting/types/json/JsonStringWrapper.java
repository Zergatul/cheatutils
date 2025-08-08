package com.zergatul.cheatutils.scripting.types.json;

import com.zergatul.scripting.type.CustomType;

@CustomType(name = "JsonString")
public class JsonStringWrapper extends JsonElementWrapper {

    private final String value;

    public JsonStringWrapper(String value) {
        this.value = value;
    }
}