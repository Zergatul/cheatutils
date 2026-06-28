package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.*;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.type.SType;

public class ListScriptTypesTool implements McpTool {

    private final JsonElement inputSchema = JsonParser.parseString("""
            {
                "type": "object",
                "properties": {},
                "additionalProperties": false
            }
            """).getAsJsonObject();

    private final JsonElement outputSchema = JsonParser.parseString("""
            {
                "type": "array",
                "items": {
                    "type": "object",
                    "properties": {
                        "interface": {
                            "type": "string",
                            "description": "Functional interface this script type is compiled into"
                        },
                        "api_types": {
                            "type": "array",
                            "description": "List of API types this script type is allowed to call"
                            "items": {
                                "type": "string"
                            }
                        },
                    },
                    "required": ["interface", "api_types"]
                }
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
    public JsonElement invoke(JsonElement arguments) {
        JsonArray result = new JsonArray();
        for (ScriptType type : ScriptType.values()) {
            CompilationParameters compilationParameters = type.createParameters();
            JsonObject item = new JsonObject();
            SType funcInterface = SType.fromJavaType(compilationParameters.getFunctionalInterface());
            item.addProperty("interface", funcInterface.toString());
            JsonArray apiTypes = new JsonArray();
            for (ApiType api : type.getApis()) {
                apiTypes.add(api.toString());
            }
            item.add("api_types", apiTypes);
            result.add(item);
        }
        return result;
    }
}