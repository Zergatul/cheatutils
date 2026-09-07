package com.zergatul.cheatutils.scripting.monaco.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.zergatul.scripting.lexer.TokenType;

import java.io.IOException;

public class TokenTypeAdapter extends TypeAdapter<TokenType> {

    @Override
    public void write(JsonWriter out, TokenType type) throws IOException {
        if (type == null) {
            out.nullValue();
        } else {
            out.value(type.ordinal());
        }
    }

    @Override
    public TokenType read(JsonReader in) {
        throw new RuntimeException();
    }
}