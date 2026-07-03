package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zergatul.cheatutils.mcp.utility.JsonObjectBuilder;
import com.zergatul.cheatutils.scripting.services.ScriptLocator;
import com.zergatul.cheatutils.scripting.services.ScriptStorageDescriptor;
import com.zergatul.cheatutils.scripting.services.ScriptWorkspaceService;

import java.io.IOException;

public class GetScriptTool implements McpTool {

    private final JsonObject inputSchema = JsonParser.parseString("""
            {
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
                "required": ["type"],
                "additionalProperties": false
            }
            """).getAsJsonObject();

    private final JsonObject outputSchema = JsonParser.parseString("""
            {
                "type": "object",
                "properties": {
                    "code": {
                        "type": ["string", "null"]
                    }
                },
                "required": ["code"],
                "additionalProperties": false
            }
            """).getAsJsonObject();

    @Override
    public String getName() {
        return "get_script";
    }

    @Override
    public String getTitle() {
        return "Get Script";
    }

    @Override
    public String getDescription() {
        return "Returns detailed information about requested script";
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
        ScriptLocator locator = new Gson().fromJson(arguments, ScriptLocator.class);
        ScriptStorageDescriptor descriptor = ScriptWorkspaceService.INSTANCE.get(locator.type());
        String code = descriptor.getCode(locator.identifier());
        return new JsonObjectBuilder()
                .withProperty("code", code)
                .build();
    }
}