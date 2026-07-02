package com.zergatul.cheatutils.mcp.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonObjectBuilder {

    private final JsonObject object = new JsonObject();

    public JsonObject build() {
        return object;
    }

    public JsonObjectBuilder withProperty(String property, int value) {
        object.add(property, new JsonPrimitive(value));
        return this;
    }

    public JsonObjectBuilder withProperty(String property, String value) {
        object.add(property, new JsonPrimitive(value));
        return this;
    }

    public JsonObjectBuilder withProperty(String property, JsonElement value) {
        object.add(property, value);
        return this;
    }
}