package com.zergatul.cheatutils.scripting.monaco;

public final class Suggestion {

    public String label;
    public String detail;
    public String documentation;
    public String insertText;
    public String kind;

    public Suggestion(String label, String detail, String documentation, String insertText, String kind) {
        this.label = label;
        this.detail = detail;
        this.documentation = documentation;
        this.insertText = insertText;
        this.kind = kind;
    }

    public Suggestion(String label, String detail, String documentation, String insertText, CompletionItemKind kind) {
        this(label, detail, documentation, insertText, kind.getName());
    }
}