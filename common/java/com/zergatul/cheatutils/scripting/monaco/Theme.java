package com.zergatul.cheatutils.scripting.monaco;

import com.zergatul.scripting.lexer.TokenType;

public abstract class Theme {
    public abstract String getTokenColor(TokenType type);
    public abstract String getPredefinedTypeColor();
    public abstract String getTypeColor();
    public abstract String getMethodColor();
    public abstract String getDescriptionColor();
    public abstract String getParameterColor();
}