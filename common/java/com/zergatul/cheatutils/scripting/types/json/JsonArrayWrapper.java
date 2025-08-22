package com.zergatul.cheatutils.scripting.types.json;

import com.google.gson.JsonArray;
import com.zergatul.scripting.Getter;
import com.zergatul.scripting.type.CustomType;

@CustomType(name = "JsonArray")
public class JsonArrayWrapper extends JsonElementWrapper {

    private final JsonArray array;

    public JsonArrayWrapper(JsonArray array) {
        this.array = array;
    }

    @Getter(name = "length")
    public int getLength() {
        return array.size();
    }
}