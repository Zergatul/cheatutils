package com.zergatul.cheatutils.scripting.types.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.zergatul.scripting.type.CustomType;

@CustomType(name = "JsonNull")
public class JsonNullWrapper extends JsonElementWrapper {

    public static final JsonElementWrapper INSTANCE = new JsonNullWrapper();

    private JsonNullWrapper() {}

    @Override
    protected JsonElement unwrap() {
        return JsonNull.INSTANCE;
    }
}