package com.zergatul.cheatutils.mcp.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class JsonObjectBuilder {

    private final JsonObject object = new JsonObject();

    public JsonObject build() {
        return object;
    }

    public JsonObjectBuilder withProperty(String property, int value) {
        object.add(property, new JsonPrimitive(value));
        return this;
    }

    public JsonObjectBuilder withProperty(String property, boolean value) {
        object.add(property, new JsonPrimitive(value));
        return this;
    }

    public JsonObjectBuilder withProperty(String property, @Nullable String value) {
        if (value == null) {
            return withProperty(property, JsonNull.INSTANCE);
        } else {
            return withProperty(property, new JsonPrimitive(value));
        }
    }

    public JsonObjectBuilder withProperty(String property, JsonElement value) {
        object.add(property, value);
        return this;
    }

    public JsonObjectBuilder withOptionalProperty(String property, @Nullable String value) {
        if (value == null) {
            return this;
        } else {
            return withProperty(property, new JsonPrimitive(value));
        }
    }

    public JsonObjectBuilder withOptionalProperty(String property, JsonElement value) {
        if (value.isJsonNull()) {
            return this;
        } else {
            return withProperty(property, value);
        }
    }
}