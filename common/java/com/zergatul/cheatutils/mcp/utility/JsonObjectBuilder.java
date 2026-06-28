package com.zergatul.cheatutils.mcp.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonObjectBuilder {

    private final JsonObject object = new JsonObject();

    public JsonObject build() {
        return object;
    }

    public JsonObjectBuilder withProperty(String property, JsonElement value) {
        object.add(property, value);
        return this;
    }
}