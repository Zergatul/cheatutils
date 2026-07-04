package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zergatul.cheatutils.mcp.utility.JsonObjectBuilder;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.cheatutils.scripting.workspace.ScriptSlot;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;

import java.io.IOException;

public class GetScriptTool implements McpTool {

    private final JsonObject inputSchema = JsonParser.parseString("""
            {
                "type": "object",
                "properties": {
                    "type": {
                        "type": "string",
                        "description": "ScriptType enum name"
                    },
                    "identifier": {
                        "type": "string",
                        "description": "Required for multi-instance script types"
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
                        "type": ["string", "null"],
                        "description": "Saved code, or null when no script exists"
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
        return "Get saved script code. Does not return failed save attempts.";
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
        ScriptRef ref = new Gson().fromJson(arguments, ScriptRef.class);
        ScriptSlot descriptor = ScriptWorkspace.INSTANCE.get(ref.type());
        String code = descriptor.getInstance(ref.identifier()).code;
        return new JsonObjectBuilder()
                .withProperty("code", code)
                .build();
    }
}