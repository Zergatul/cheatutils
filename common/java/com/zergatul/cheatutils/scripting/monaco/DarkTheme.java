package com.zergatul.cheatutils.scripting.monaco;

import com.zergatul.scripting.highlighting.SemanticTokenType;
import com.zergatul.scripting.hover.Theme;

public class DarkTheme extends Theme {

    private static final String KEYWORD = "569CD6";
    private static final String METHOD = "56A8F5";
    private static final String PROPERTY = "C77DBB";
    private static final String IDENTIFIER = "DCDCAA";
    private static final String TYPES = "4EC9B0";
    private static final String BRACKETS = "FFD700";
    private static final String OPERATORS = "D4D4D4";
    private static final String SEPARATORS = "CCCCCC";
    private static final String ARROW = "75BE76";
    private static final String NUMBERS = "B5CEA8";
    private static final String STRINGS = "CE9178";
    private static final String COMMENTS = "6A9955";

    @Override
    public String getTokenColor(SemanticTokenType type) {
        return switch (type) {
            case KEYWORD -> KEYWORD;
            case METHOD -> METHOD;
            case PROPERTY -> PROPERTY;
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
        return "F1F1F1";
    }

    @Override
    public String getParameterColor() {
        return "9CDCFE";
    }
}