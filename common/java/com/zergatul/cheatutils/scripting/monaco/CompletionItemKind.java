package com.zergatul.cheatutils.scripting.monaco;

public enum CompletionItemKind {
    METHOD("Method"),
    PROPERTY("Property"),
    VARIABLE("Variable"),
    KEYWORD("Keyword"),
    FUNCTION("Function"),
    STRUCT("Struct"),
    CLASS("Class"),
    CONSTANT("Constant"),
    MODULE("Module"),
    VALUE("Value");

    private final String name;

    CompletionItemKind(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}