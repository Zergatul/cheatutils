package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.zergatul.cheatutils.wrappers.ClassRemapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ClassTypeAdapter extends TypeAdapter<Class<?>> {

    private static final Logger logger = LogManager.getLogger(ClassTypeAdapter.class);

    @Override
    public void write(JsonWriter out, Class<?> value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            String name = value.getName();
            try {
                name = ClassRemapper.fromObf(name);
            } catch (RuntimeException | LinkageError ignored) {
                // Standalone smoke tests do not initialize a complete mod-loader environment.
            }
            out.value(name);
        }
    }

    @Override
    public Class<?> read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String value = in.nextString();
        String runtimeName = value;
        try {
            runtimeName = ClassRemapper.toObf(value);
        } catch (RuntimeException | LinkageError ignored) {
            // Standalone smoke tests do not initialize a complete mod-loader environment.
        }

        try {
            return Class.forName(runtimeName);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            if (!runtimeName.equals(value)) {
                try {
                    return Class.forName(value);
                } catch (ReflectiveOperationException | LinkageError ignoredAgain) {
                    // Handled below.
                }
            }
        }

        logger.warn("Cannot resolve configured class '{}'. The corresponding config entry will be discarded.", value);
        return null;
    }
}