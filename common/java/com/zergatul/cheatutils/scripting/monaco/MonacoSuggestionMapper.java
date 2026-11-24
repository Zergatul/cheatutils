package com.zergatul.cheatutils.scripting.monaco;

import com.zergatul.scripting.completion.SuggestionInfo;
import com.zergatul.scripting.completion.SuggestionKind;
import com.zergatul.scripting.completion.SuggestionMapper;

public class MonacoSuggestionMapper implements SuggestionMapper<Suggestion> {

    @Override
    public Suggestion map(SuggestionInfo suggestion) {
        return new Suggestion(
                suggestion.label(),
                suggestion.detail(),
                suggestion.documentation(),
                suggestion.insertText(),
                getKind(suggestion.kind()));
    }

    private CompletionItemKind getKind(SuggestionKind kind) {
        return switch (kind) {
            case KEYWORD -> CompletionItemKind.KEYWORD;
            case TYPE -> CompletionItemKind.CLASS;
            case PACKAGE -> CompletionItemKind.MODULE;
            case PROPERTY -> CompletionItemKind.PROPERTY;
            case METHOD -> CompletionItemKind.METHOD;
            case CONSTANT -> CompletionItemKind.CONSTANT;
            case VARIABLE -> CompletionItemKind.VARIABLE;
            case FUNCTION -> CompletionItemKind.FUNCTION;
        };
    }
}