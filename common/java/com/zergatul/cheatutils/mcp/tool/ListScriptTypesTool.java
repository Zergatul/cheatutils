package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.*;
import com.zergatul.cheatutils.mcp.utility.JsonObjectBuilder;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.services.ScriptWorkspaceService;
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
                                    "description": "Script type"
                                },
                                "interface": {
                                    "type": "string",
                                    "description": "Functional interface this script type is compiled into"
                                },
                                "api_types": {
                                    "type": "array",
                                    "description": "List of API types this script type is allowed to call",
                                    "items": {
                                        "type": "string"
                                    }
                                }
                            },
                            "required": ["type", "interface", "api_types"]
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
        return "Returns list of all supported script types";
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
        for (ScriptType type : ScriptWorkspaceService.INSTANCE.getSupportedTypes()) {
            CompilationParameters compilationParameters = type.createParameters();
            JsonObject item = new JsonObject();
            item.addProperty("type", type.name());
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