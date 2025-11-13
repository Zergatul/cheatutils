package com.zergatul.cheatutils.scripting.types.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.zergatul.scripting.Getter;
import com.zergatul.scripting.IndexGetter;
import com.zergatul.scripting.IndexSetter;
import com.zergatul.scripting.type.CustomType;

@CustomType(name = "JsonArray")
public class JsonArrayWrapper extends JsonElementWrapper {

    private final JsonArray array;

    public JsonArrayWrapper() {
        this.array = new JsonArray();
    }

    protected JsonArrayWrapper(JsonArray array) {
        this.array = array;
    }

    public void add(JsonElementWrapper element) {
        if (element == null) {
            return;
        }
        if (element == JsonInvalidWrapper.INSTANCE) {
            return;
        }
        array.add(element.unwrap());
    }

    @Getter(name = "length")
    public int getLength() {
        return array.size();
    }

    @IndexGetter
    public JsonElementWrapper get(int index) {
        if (index < 0 || index >= array.size()) {
            return JsonInvalidWrapper.INSTANCE;
        } else {
            return from(array.get(index));
        }
    }

    @IndexSetter
    public void set(int index, JsonElementWrapper element) {
        if (index < 0 || index >= array.size()) {
            return;
        }
        if (element == null) {
            return;
        }
        if (element == JsonInvalidWrapper.INSTANCE) {
            return;
        }
        array.set(index, element.unwrap());
    }

    @Override
    protected JsonElement unwrap() {
        return array;
    }
}