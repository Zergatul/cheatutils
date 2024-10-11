package com.zergatul.cheatutils.scripting.monaco;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class TokenTypeEx {

    public static final String[] VALUES;
    public static final int CUSTOM_TYPE_INDEX;

    static {
        List<String> list = new ArrayList<>();
        for (TokenType type : TokenType.values()) {
            list.add(type.name());
        }

        CUSTOM_TYPE_INDEX = list.size();
        list.add("CUSTOM_TYPE");

        VALUES = list.toArray(String[]::new);
    }

    public static int indexOf(TokenType type) {
        return type.ordinal();
    }

    public static String getTokenColor(String type, Theme theme) {
        for (TokenType raw : TokenType.values()) {
            if (raw.name().equals(type)) {
                return theme.getTokenColor(raw);
            }
        }
        if (type.equals("CUSTOM_TYPE")) {
            return theme.getTypeColor();
        }
        throw new InternalException();
    }
}