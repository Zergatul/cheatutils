package com.zergatul.cheatutils.mcp.utility;

import com.zergatul.scripting.completion.SuggestionInfo;
import com.zergatul.scripting.completion.SuggestionMapper;

import java.util.Locale;

public class McpSuggestionMapper implements SuggestionMapper<McpSuggestion> {

    @Override
    public McpSuggestion map(SuggestionInfo suggestion) {
        boolean hasSignature = switch (suggestion.kind()) {
            case METHOD, FUNCTION -> true;
            default -> false;
        };
        return new McpSuggestion(
                suggestion.label(),
                suggestion.kind().toString().toLowerCase(Locale.ROOT),
                hasSignature ? null : suggestion.detail(),
                hasSignature ? suggestion.detail() : null,
                suggestion.documentation());
    }
}