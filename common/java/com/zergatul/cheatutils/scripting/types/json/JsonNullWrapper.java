package com.zergatul.cheatutils.scripting.types.json;

import com.zergatul.scripting.type.CustomType;

@CustomType(name = "JsonNull")
public class JsonNullWrapper extends JsonElementWrapper {

    public static final JsonElementWrapper INSTANCE = new JsonNullWrapper();

    private JsonNullWrapper() {}
}