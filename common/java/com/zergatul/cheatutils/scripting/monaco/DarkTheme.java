package com.zergatul.cheatutils.scripting.monaco;

import com.zergatul.scripting.lexer.TokenType;

public class DarkTheme extends Theme {

    private static final String KEYWORD = "569CD6";
    private static final String IDENTIFIER = "DCDCAA";
    private static final String TYPES = "4EC9B0";
    private static final String BRACKETS = "FFD700";
    private static final String OPERATORS = "D4D4D4";
    private static final String SEPARATORS = "CCCCCC";
    private static final String NUMBERS = "B5CEA8";
    private static final String STRINGS = "CE9178";
    private static final String COMMENTS = "6A9955";
    private static final String INVALID = "FF0000";

    @Override
    public String getTokenColor(TokenType type) {
        return switch (type) {
            case IDENTIFIER -> IDENTIFIER;
            case LEFT_PARENTHESES, LEFT_CURLY_BRACKET, LEFT_SQUARE_BRACKET, RIGHT_PARENTHESES, RIGHT_CURLY_BRACKET,
                 RIGHT_SQUARE_BRACKET -> BRACKETS;
            case DOT, COMMA, SEMICOLON, COLON -> SEPARATORS;
            case PLUS, PLUS_PLUS, PLUS_EQUAL, MINUS, MINUS_MINUS, MINUS_EQUAL, ASTERISK, ASTERISK_EQUAL, SLASH,
                 SLASH_EQUAL, PERCENT, PERCENT_EQUAL, AMPERSAND, AMPERSAND_AMPERSAND, AMPERSAND_EQUAL, PIPE, PIPE_PIPE,
                 PIPE_EQUAL, EQUAL, EQUAL_EQUAL, EQUAL_GREATER, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL, EXCLAMATION,
                 EXCLAMATION_EQUAL, QUESTION -> OPERATORS;
            case BOOLEAN, INT, CHAR, FLOAT, STRING, IF, ELSE, BREAK, CONTINUE, WHILE, FOR, FOREACH, FALSE, TRUE, IN,
                 NEW, REF, RETURN, STATIC, VOID, ASYNC, AWAIT -> KEYWORD;
            case INTEGER_LITERAL, FLOAT_LITERAL, INVALID_NUMBER -> NUMBERS;
            case CHAR_LITERAL, STRING_LITERAL -> STRINGS;
            case WHITESPACE, LINE_BREAK, END_OF_FILE, INVALID -> INVALID;
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