package com.zergatul.cheatutils.scripting.monaco.adapters;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.zergatul.scripting.TextRange;

import java.io.IOException;

public class TextRangeTypeAdapterFactory implements TypeAdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (type.getRawType().isAssignableFrom(TextRange.class)) {
            return (TypeAdapter<T>) new TextRangeTypeAdapter();
        }
        return null;
    }

    private static class TextRangeTypeAdapter extends TypeAdapter<TextRange> {

        @Override
        public void write(JsonWriter out, TextRange value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.beginObject();
                out.name("line1").value(value.getLine1());
                out.name("column1").value(value.getColumn1());
                out.name("line2").value(value.getLine2());
                out.name("column2").value(value.getColumn2());
                out.name("length").value(value.getLength());
                out.endObject();
            }
        }

        @Override
        public TextRange read(JsonReader in) {
            throw new RuntimeException();
        }
    }
}