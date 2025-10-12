package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.*;
import com.zergatul.cheatutils.collections.ImmutableList;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ImmutableListSerializer implements JsonSerializer<ImmutableList<?>>, JsonDeserializer<ImmutableList<?>> {

    @Override
    public JsonElement serialize(ImmutableList<?> list, Type typeOfSrc, JsonSerializationContext context) throws JsonParseException {
        if (list == null) {
            return JsonNull.INSTANCE;
        }

        Type elementsType = getElementsType(typeOfSrc);

        JsonArray array = new JsonArray();
        for (Object value : list) {
            array.add(context.serialize(value, elementsType));
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

        Type elementType = getElementsType(typeOfT);

        JsonArray array = (JsonArray) json;
        ImmutableList list = ImmutableList.empty();
        for (JsonElement element : array) {
            list = list.add(context.deserialize(element, elementType));
        }

        return list;
    }

    private Type getElementsType(Type type) {
        if (type instanceof ParameterizedType parameterized) {
            return parameterized.getActualTypeArguments()[0];
        } else {
            throw new JsonParseException("Unexpected type.");
        }
    }
}