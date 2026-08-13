package com.zergatul.cheatutils.scripting.monaco;

public enum CompletionItemKind {
    METHOD("Method"),
    PROPERTY("Property"),
    VARIABLE("Variable"),
    KEYWORD("Keyword"),
    FUNCTION("Function"),
    CLASS("Class"),
    CONSTANT("Constant"),
    MODULE("Module");

    private final String name;

    CompletionItemKind(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}