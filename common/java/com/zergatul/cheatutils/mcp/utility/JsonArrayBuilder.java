package com.zergatul.cheatutils.mcp.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.stream.Stream;

public class JsonArrayBuilder {

    private final JsonArray array = new JsonArray();

    public JsonArray build() {
        return array;
    }

    public <T extends JsonElement> JsonArrayBuilder withItems(Stream<T> stream) {
        stream.forEach(array::add);
        return this;
    }
}