package com.zergatul.cheatutils.scripting.types.json;

import com.zergatul.scripting.type.CustomType;

@CustomType(name = "JsonInvalid")
public class JsonInvalidWrapper extends JsonElementWrapper {

    public static final JsonElementWrapper INSTANCE = new JsonInvalidWrapper();

    private JsonInvalidWrapper() {}
}