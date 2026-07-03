package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zergatul.cheatutils.mcp.utility.JsonArrayBuilder;
import com.zergatul.cheatutils.mcp.utility.JsonObjectBuilder;
import com.zergatul.cheatutils.scripting.services.ScriptWorkspaceService;

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
                                    "description": "Script type"
                                },
                                "identifier": {
                                    "type": "string",
                                    "description": "Script identifier"
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
        return "Returns list of all scripts";
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
                        .withItems(ScriptWorkspaceService.INSTANCE.getAll()
                                .stream().map(descriptor -> {
                                    var item = new JsonObject();
                                    item.addProperty("type", descriptor.getLocator().type().toString());
                                    if (descriptor.getLocator().identifier() != null) {
                                        item.addProperty("identifier", descriptor.getLocator().identifier());
                                    }
                                    return item;
                                }))
                        .build())
                .build();
    }
}