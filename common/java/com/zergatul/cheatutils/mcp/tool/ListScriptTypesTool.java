package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.*;
import com.zergatul.cheatutils.mcp.utility.JsonObjectBuilder;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.type.SType;

public class ListScriptTypesTool implements McpTool {

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
                                "module_name": {
                                    "type": "string",
                                    "description": "UI module name"
                                },
                                "documentation_uri": {
                                    "type": "string",
                                    "description": "Read this before writing this script type"
                                },
                                "interface": {
                                    "type": "string",
                                    "description": "Script signature"
                                },
                                "api_types": {
                                    "type": "array",
                                    "description": "Allowed @ApiVisibility values",
                                    "items": {
                                        "type": "string"
                                    }
                                }
                            },
                            "required": ["type", "module_name", "documentation_uri", "interface", "api_types"]
                        }
                    }
                },
                "required": ["items"],
                "additionalProperties": false
            }
            """).getAsJsonObject();

    @Override
    public String getName() {
        return "list_script_types";
    }

    @Override
    public String getTitle() {
        return "List Script Types";
    }

    @Override
    public String getDescription() {
        return "List supported script types and their documentation URIs.";
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
        JsonArray result = new JsonArray();
        for (ScriptType type : ScriptWorkspace.INSTANCE.getSupportedTypes()) {
            CompilationParameters compilationParameters = type.createParameters();
            JsonObject item = new JsonObject();
            item.addProperty("type", type.name());
            item.addProperty("module_name", type.getModuleName());
            item.addProperty("documentation_uri", "cheatutils://docs/script-type/" + type.name());
            SType funcInterface = SType.fromJavaType(compilationParameters.getFunctionalInterface());
            item.addProperty("interface", funcInterface.toString());
            JsonArray apiTypes = new JsonArray();
            for (ApiType api : type.getApis()) {
                apiTypes.add(api.toString());
            }
            item.add("api_types", apiTypes);
            result.add(item);
        }
        return new JsonObjectBuilder()
                .withProperty("items", result)
                .build();
    }
}