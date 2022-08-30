package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.zergatul.cheatutils.configs.ClassRemapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ClassTypeAdapter extends TypeAdapter<Class> {

    private Logger logger = LogManager.getLogger(ClassTypeAdapter.class);

    @Override
    public void write(JsonWriter out, Class value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(ClassRemapper.fromObf(value.getName()));
        }
    }

    @Override
    public Class read(JsonReader in) throws IOException {
        String value = in.nextString();
        if (value == null) {
            return null;
        } else {
            try {
                return Class.forName(ClassRemapper.toObf(value));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}