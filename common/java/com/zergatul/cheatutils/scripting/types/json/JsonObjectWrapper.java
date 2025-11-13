package com.zergatul.cheatutils.scripting.types.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zergatul.scripting.Getter;
import com.zergatul.scripting.IndexGetter;
import com.zergatul.scripting.IndexSetter;
import com.zergatul.scripting.type.CustomType;

@CustomType(name = "JsonObject")
public class JsonObjectWrapper extends JsonElementWrapper {

    private final JsonObject object;

    public JsonObjectWrapper() {
        this.object = new JsonObject();
    }

    public JsonObjectWrapper(JsonObject object) {
        this.object = object;
    }

    @IndexGetter
    public JsonElementWrapper get(String key) {
        JsonElement element = object.get(key);
        if (element == null) {
            return JsonInvalidWrapper.INSTANCE;
        }
        return JsonElementWrapper.from(element);
    }

    @IndexSetter
    public void set(String key, JsonElementWrapper element) {
        if (key == null) {
            return;
        }
        if (element == null) {
            return;
        }
        if (element == JsonInvalidWrapper.INSTANCE) {
            return;
        }
        object.add(key, element.unwrap());
    }

    @Getter(name = "count")
    public int getCount() {
        return object.size();
    }

    @Getter(name = "keys")
    public String[] getKeys() {
        return object.keySet().toArray(String[]::new);
    }

    public boolean has(String key) {
        return object.has(key);
    }

    @Override
    protected JsonElement unwrap() {
        return object;
    }
}