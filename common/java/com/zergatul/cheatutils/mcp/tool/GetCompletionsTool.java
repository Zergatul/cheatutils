package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zergatul.cheatutils.mcp.utility.JsonArrayBuilder;
import com.zergatul.cheatutils.mcp.utility.JsonObjectBuilder;
import com.zergatul.cheatutils.mcp.utility.McpSuggestion;
import com.zergatul.cheatutils.mcp.utility.McpSuggestionMapper;
import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.scripting.analysis.AnalysisResult;
import com.zergatul.scripting.analysis.Analyzer;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.completion.CompletionProviderFactory;
import com.zergatul.scripting.completion.MappedSuggestionFactory;
import com.zergatul.scripting.completion.SuggestionInfoFactory;
import com.zergatul.scripting.formatting.TypeDisplayFormatter;

import java.io.IOException;
import java.util.List;

public class GetCompletionsTool implements McpTool {

    private static final String CURSOR = "<cursor>";

    private final JsonObject inputSchema = JsonParser.parseString("""
            {
                "type": "object",
                "properties": {
                    "type": {
                        "type": "string",
                        "description": "ScriptType enum name"
                    },
                    "code": {
                        "type": "string",
                        "description": "Full script source containing a <cursor> marker at the completion point"
                    }
                },
                "required": ["type", "code"],
                "additionalProperties": false
            }
            """).getAsJsonObject();

    private final JsonObject outputSchema = JsonParser.parseString("""
            {
                "type": "object",
                "properties": {
                    "items": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "properties": {
                                "name": {
                                    "type": "string",
                                    "description": "Suggested identifier, keyword, type, method, or property name to insert at cursor"
                                },
                                "kind": {
                                    "type": "string",
                                    "description": "Suggestion category, such as keyword, type, property, method, constant, variable, or function"
                                },
                                "type": {
                                    "type": "string",
                                    "description": "Value type, return type, or receiver type when available"
                                },
                                "signature": {
                                    "type": "string",
                                    "description": "Method or function signature when available"
                                },
                                "documentation": {
                                    "type": "string",
                                    "description": "Short documentation when available"
                                }
                            },
                            "required": ["name", "kind"],
                            "additionalProperties": false
                        }
                    }
                },
                "required": ["items"],
                "additionalProperties": false
            }
            """).getAsJsonObject();

    @Override
    public String getName() {
        return "get_completions";
    }

    @Override
    public String getTitle() {
        return "Get Completions";
    }

    @Override
    public String getDescription() {
        return
                "Returns completion suggestions for a CheatUtils script at the <cursor> marker. " +
                "Pass the full script source in code with exactly one <cursor> where completion is needed. " +
                "Use this to discover valid keywords, types, variables, functions, properties, and methods available in the current script context. " +
                "This analyzes code without saving or executing it. " +
                "Completions are most reliable for member access like obj.<cursor>; results may be incomplete and are not a full source of truth.";
    }

    @Override
    public JsonObject getInputSchema() {
        return inputSchema;
    }

    @Override
    public JsonObject getOutputSchema() {
        return outputSchema;
    }

    @Override
    public McpToolCallResult invoke(JsonElement arguments) throws IOException {
        ScriptType scriptType = ScriptType.valueOf(arguments.getAsJsonObject().get("type").getAsString());
        String code = arguments.getAsJsonObject().get("code").getAsString();
        if (!code.contains(CURSOR)) {
            return McpToolCallResult.error("Invalid code: include exactly one <cursor> marker at the completion point.");
        }

        int line = -1, column = -1;
        String[] lines = code.lines().toArray(String[]::new);
        for (int i = 0; i < lines.length; i++) {
            int index = lines[i].indexOf(CURSOR);
            if (index >= 0) {
                line = i + 1;
                column = index + 1;
                break;
            }
        }
        if (line == -1) {
            throw new IllegalStateException();
        }

        code = code.replace(CURSOR, "");

        TypeDisplayFormatter typeFormatter = new TypeDisplayFormatter(clazz ->
                clazz.getName().startsWith("com.zergatul.cheatutils.scripting") ? clazz.getSimpleName() : clazz.getName());
        CompletionProviderFactory<McpSuggestion> completionProviderFactory = new CompletionProviderFactory<>(
                new MappedSuggestionFactory<>(
                        new SuggestionInfoFactory(typeFormatter),
                        new McpSuggestionMapper()));
        CompilationParameters parameters = ScriptCompilerRegistry.INSTANCE.getParameters(scriptType);
        AnalysisResult analysisResult = new Analyzer().analyze(code, parameters);
        List<McpSuggestion> suggestions = completionProviderFactory.getSuggestions(parameters, analysisResult.binderOutput(), line, column);
        JsonObject result = new JsonObjectBuilder()
                .withProperty("items", new JsonArrayBuilder()
                        .withItems(suggestions.stream()
                                .map(McpSuggestion::toJson))
                        .build())
                .build();
        return McpToolCallResult.success(result);
    }
}