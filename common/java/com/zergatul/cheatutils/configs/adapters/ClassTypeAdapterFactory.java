package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.zergatul.cheatutils.wrappers.ClassRemapper;

import java.io.IOException;

public class ClassTypeAdapterFactory implements TypeAdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (type.getRawType() == Class.class) {
            return (TypeAdapter<T>) new ClassTypeAdapter();
        }
        return null;
    }

    private static class ClassTypeAdapter extends TypeAdapter<Class<?>> {

        @Override
        public void write(JsonWriter out, Class<?> value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(ClassRemapper.fromObf(value.getName()));
            }
        }

        @Override
        public Class<?> read(JsonReader in) throws IOException {
            String value = in.nextString();
            if (value == null) {
                return null;
            } else {
                try {
                    return Class.forName(ClassRemapper.toObf(value));
                }
                catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
    }
}