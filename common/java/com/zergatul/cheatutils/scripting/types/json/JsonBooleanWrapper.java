package com.zergatul.cheatutils.scripting.types.json;

import com.zergatul.scripting.type.CustomType;

@CustomType(name = "JsonBoolean")
public class JsonBooleanWrapper extends JsonElementWrapper {

    private static final JsonBooleanWrapper FALSE = new JsonBooleanWrapper(false);
    private static final JsonBooleanWrapper TRUE = new JsonBooleanWrapper(true);

    private final boolean value;

    private JsonBooleanWrapper(boolean value) {
        this.value = value;
    }

    public static JsonBooleanWrapper from(boolean value) {
        return value ? TRUE : FALSE;
    }
}