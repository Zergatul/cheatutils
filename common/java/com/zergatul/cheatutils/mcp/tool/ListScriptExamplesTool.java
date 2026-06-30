package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ListScriptExamplesTool implements McpTool {

    private final JsonObject inputSchema = JsonParser.parseString("""
            {
                "type": "object",
                "properties": {},
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
                            "properties": {
                                "resource": {
                                    "type": "string"
                                },
                                "description": {
                                    "type": "string"
                                }
                            },
                            "required": ["resource", "description"],
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
        return "list_script_examples";
    }

    @Override
    public String getTitle() {
        return "List Script Examples";
    }

    @Override
    public String getDescription() {
        return "Returns list of available example scripts MCP resources";
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
    public JsonObject invoke(JsonElement arguments) {
        return null;
    }
}