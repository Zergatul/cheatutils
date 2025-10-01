package com.zergatul.cheatutils.scripting.monaco;

import com.zergatul.scripting.highlighting.SemanticTokenType;
import com.zergatul.scripting.hover.Theme;

public class WhiteTheme extends Theme {

    private static final String KEYWORD = "0000FF";
    private static final String IDENTIFIER = "1F377F";
    private static final String TYPES = "2B91AF";
    private static final String BRACKETS = "000000";
    private static final String OPERATORS = "000000";
    private static final String SEPARATORS = "000000";
    private static final String NUMBERS = "000000";
    private static final String STRINGS = "A31515";
    private static final String COMMENTS = "008000";

    @Override
    public String getTokenColor(SemanticTokenType type) {
        return switch (type) {
            case KEYWORD -> KEYWORD;
            case METHOD -> "000000";
            case PROPERTY -> "000000";
            case IDENTIFIER -> IDENTIFIER;
            case TYPE -> TYPES;
            case BRACKET -> BRACKETS;
            case SEPARATOR -> SEPARATORS;
            case OPERATOR -> OPERATORS;
            case NUMBER -> NUMBERS;
            case STRING -> STRINGS;
            case COMMENT -> COMMENTS;
        };
    }

    @Override
    public String getPredefinedTypeColor() {
        return KEYWORD;
    }

    @Override
    public String getTypeColor() {
        return TYPES;
    }

    @Override
    public String getMethodColor() {
        return IDENTIFIER;
    }

    @Override
    public String getDescriptionColor() {
        return "1E1E1E";
    }

    @Override
    public String getParameterColor() {
        return "1F377F";
    }
}