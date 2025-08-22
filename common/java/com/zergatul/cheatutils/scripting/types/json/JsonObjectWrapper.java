package com.zergatul.cheatutils.scripting.types.json;

import com.google.gson.JsonObject;

public class JsonObjectWrapper extends JsonElementWrapper {

    private final JsonObject object;

    public JsonObjectWrapper(JsonObject object) {
        this.object = object;
    }

}