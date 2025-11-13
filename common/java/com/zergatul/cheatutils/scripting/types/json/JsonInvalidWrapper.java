package com.zergatul.cheatutils.scripting.types.json;

import com.google.gson.JsonElement;
import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.type.CustomType;

@CustomType(name = "JsonInvalid")
public class JsonInvalidWrapper extends JsonElementWrapper {

    public static final JsonElementWrapper INSTANCE = new JsonInvalidWrapper();

    private JsonInvalidWrapper() {}

    @Override
    protected JsonElement unwrap() {
        throw new InternalException();
    }
}