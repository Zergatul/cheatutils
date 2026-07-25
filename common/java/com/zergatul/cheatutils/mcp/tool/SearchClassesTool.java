package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zergatul.cheatutils.mcp.utility.JsonObjectBuilder;
import com.zergatul.cheatutils.utils.ClassPathExplorer;

import java.util.List;
import java.util.regex.PatternSyntaxException;

public class SearchClassesTool implements McpTool {

    private static final int MAX_QUERY_LENGTH = 256;
    private static final int MAX_LIMIT = 100;

    private final JsonObject inputSchema = JsonParser.parseString("""
            {
                "type": "object",
                "properties": {
                    "query": {
                        "type": "string",
                        "description": "Case-insensitive Java regex searched within fully qualified class names; use | to search for alternatives",
                        "minLength": 1,
                        "maxLength": 256
                    },
                    "limit": {
                        "type": "integer",
                        "description": "Maximum number of class names to return",
                        "minimum": 1,
                        "maximum": 100
                    }
                },
                "required": ["query", "limit"],
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
                            "type": "string",
                            "description": "Fully qualified class name"
                        }
                    }
                },
                "required": ["items"],
                "additionalProperties": false
            }
            """).getAsJsonObject();

    @Override
    public String getName() {
        return "search_classes";
    }

    @Override
    public String getTitle() {
        return "Search Classes";
    }

    @Override
    public String getDescription() {
        return "Search potentially accessible Java classes by fully qualified name for use with Java interop.";
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
    public McpToolCallResult invoke(JsonElement arguments) {
        if (arguments == null || !arguments.isJsonObject()) {
            return McpToolCallResult.error("Invalid arguments: expected object with string property 'query' and integer property 'limit'.");
        }

        JsonObject object = arguments.getAsJsonObject();
        JsonElement queryElement = object.get("query");
        if (queryElement == null || !queryElement.isJsonPrimitive() || !queryElement.getAsJsonPrimitive().isString()) {
            return McpToolCallResult.error("Invalid arguments: expected string property 'query'.");
        }

        String query = queryElement.getAsString();
        if (query.isEmpty() || query.length() > MAX_QUERY_LENGTH) {
            return McpToolCallResult.error("Invalid arguments: 'query' length must be between 1 and 256.");
        }

        JsonElement limitElement = object.get("limit");
        if (limitElement == null || !limitElement.isJsonPrimitive() || !limitElement.getAsJsonPrimitive().isNumber()) {
            return McpToolCallResult.error("Invalid arguments: expected integer property 'limit'.");
        }

        int limit;
        try {
            limit = Integer.parseInt(limitElement.getAsString());
        } catch (NumberFormatException e) {
            return McpToolCallResult.error("Invalid arguments: expected integer property 'limit'.");
        }

        if (limit < 1 || limit > MAX_LIMIT) {
            return McpToolCallResult.error("Invalid arguments: 'limit' must be between 1 and 100.");
        }

        List<String> classes;
        try {
            classes = ClassPathExplorer.INSTANCE.find(query, limit);
        } catch (PatternSyntaxException e) {
            return McpToolCallResult.error("Invalid regular expression: " + e.getDescription());
        }

        JsonArray items = new JsonArray();
        for (String className : classes) {
            items.add(className);
        }

        return McpToolCallResult.success(new JsonObjectBuilder()
                .withProperty("items", items)
                .build());
    }
}