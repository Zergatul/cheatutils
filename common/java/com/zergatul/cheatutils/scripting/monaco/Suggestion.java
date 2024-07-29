package com.zergatul.cheatutils.scripting.monaco;

public record Suggestion(String label, String detail, String documentation, String insertText, String kind) {

    public Suggestion(String label, String detail, String documentation, String insertText, CompletionItemKind kind) {
        this(label, detail, documentation, insertText, kind.getName());
    }
}