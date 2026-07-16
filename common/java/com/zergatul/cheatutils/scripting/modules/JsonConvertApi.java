package com.zergatul.cheatutils.scripting.modules;

import com.google.gson.Gson;
import com.zergatul.scripting.MethodDescription;
import com.zergatul.scripting.runtime.RuntimeType;

public class JsonConvertApi {

    private final Gson gson = new Gson();

    public String serialize(Object object) {
        return gson.toJson(object);
    }

    @MethodDescription("Deserializes JSON to the type specified with #type(...).")
    public Object deserialize(String json, RuntimeType type) {
        return gson.fromJson(json, type.getJavaType());
    }
}