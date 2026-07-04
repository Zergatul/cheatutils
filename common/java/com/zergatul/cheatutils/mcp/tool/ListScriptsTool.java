package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zergatul.cheatutils.mcp.utility.JsonArrayBuilder;
import com.zergatul.cheatutils.mcp.utility.JsonObjectBuilder;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;

import java.io.IOException;

public class ListScriptsTool implements McpTool {

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
                            "type": "object",
                            "properties": {
                                "type": {
                                    "type": "string",
                                    "description": "ScriptType enum name"
                                },
                                "identifier": {
                                    "type": "string",
                                    "description": "Present for multi-instance script types"
                                }
                            },
                            "required": ["type"]
                        }
                    }
                },
                "required": ["items"],
                "additionalProperties": false
            }
            """).getAsJsonObject();

    @Override
    public String getName() {
        return "list_scripts";
    }

    @Override
    public String getTitle() {
        return "List Scripts";
    }

    @Override
    public String getDescription() {
        return "List configured script slots.";
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
    public JsonObject invoke(JsonElement arguments) throws IOException {
        return new JsonObjectBuilder()
                .withProperty("items", new JsonArrayBuilder()
                        .withItems(ScriptWorkspace.INSTANCE.getAllInstances()
                                .stream().map(instance -> {
                                    JsonObject item = new JsonObject();
                                    item.addProperty("type", instance.ref.type().toString());
                                    if (instance.ref.identifier() != null) {
                                        item.addProperty("identifier", instance.ref.identifier());
                                    }
                                    return item;
                                }))
                        .build())
                .build();
    }
}