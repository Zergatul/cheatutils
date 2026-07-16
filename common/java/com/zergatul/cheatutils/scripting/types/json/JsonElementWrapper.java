package com.zergatul.cheatutils.scripting.types.json;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.zergatul.scripting.MethodDescription;
import com.zergatul.scripting.type.CustomType;

import java.io.IOException;
import java.io.StringWriter;

@CustomType(name = "JsonElement")
public abstract class JsonElementWrapper {

    @MethodDescription("Returns JsonInvalid when the input is not valid JSON.")
    public static JsonElementWrapper parse(String json) {
        try {
            return from(JsonParser.parseString(json));
        } catch (JsonParseException e) {
            return JsonInvalidWrapper.INSTANCE;
        }
    }

    @Override
    public String toString() {
        return unwrap().toString();
    }

    public String toStringFormatted() {
        return toStringFormatted(4);
    }

    @MethodDescription("Pretty-prints using the specified non-negative number of spaces per indent.")
    public String toStringFormatted(int spaces) {
        try {
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(stringWriter);
            jsonWriter.setFormattingStyle(FormattingStyle.PRETTY.withIndent(" ".repeat(spaces)));
            jsonWriter.setStrictness(Strictness.LENIENT);
            Streams.write(unwrap(), jsonWriter);
            return stringWriter.toString();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    protected abstract JsonElement unwrap();

    protected static JsonElementWrapper from(JsonElement element) {
        if (element.isJsonArray()) {
            return new JsonArrayWrapper(element.getAsJsonArray());
        }
        if (element.isJsonObject()) {
            return new JsonObjectWrapper(element.getAsJsonObject());
        }
        if (element.isJsonNull()) {
            return JsonNullWrapper.INSTANCE;
        }
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return new JsonBooleanWrapper(primitive.getAsBoolean());
            }
            if (primitive.isNumber()) {
                return new JsonNumberWrapper(primitive.getAsDouble());
            }
            if (primitive.isString()) {
                return new JsonStringWrapper(primitive.getAsString());
            }
        }
        return JsonInvalidWrapper.INSTANCE;
    }
}