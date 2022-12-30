package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.*;
import com.zergatul.cheatutils.collections.ImmutableList;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ImmutableListSerializer implements JsonSerializer<ImmutableList<?>>, JsonDeserializer<ImmutableList<?>> {

    @Override
    public JsonElement serialize(ImmutableList<?> list, Type typeOfSrc, JsonSerializationContext context) {
        if (list == null) {
            return JsonNull.INSTANCE;
        }

        JsonArray array = new JsonArray();
        for (Object value: list) {
            array.add(context.serialize(value));
        }
        return array;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ImmutableList<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonNull()) {
            return null;
        }

        if (!json.isJsonArray()) {
            throw new JsonParseException("Array expected.");
        }

        JsonArray array = (JsonArray) json;

        Type elementType = ((ParameterizedType) typeOfT).getActualTypeArguments()[0];
        Constructor<ImmutableList> constructor;
        try {
            constructor = ImmutableList.class.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new JsonParseException("Cannot find constructor of ImmutableList.");
        }

        ImmutableList list;
        try {
            list = constructor.newInstance();
        } catch (Exception e) {
            throw new JsonParseException("Cannot invoke constructor of ImmutableList.");
        }

        for (JsonElement element: array) {
            list = list.add(context.deserialize(element, elementType));
        }

        return list;
    }
}