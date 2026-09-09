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
        switch (kind) {
            case KEYWORD:
                return CompletionItemKind.KEYWORD;
            case TYPE:
                return CompletionItemKind.CLASS;
            case PACKAGE:
                return CompletionItemKind.MODULE;
            case PROPERTY:
                return CompletionItemKind.PROPERTY;
            case METHOD:
                return CompletionItemKind.METHOD;
            case CONSTANT:
                return CompletionItemKind.CONSTANT;
            case VARIABLE:
                return CompletionItemKind.VARIABLE;
            case FUNCTION:
                return CompletionItemKind.FUNCTION;
            default:
                throw new IllegalArgumentException();
        }
    }
}