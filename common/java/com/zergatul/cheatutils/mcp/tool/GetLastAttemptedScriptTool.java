package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zergatul.cheatutils.mcp.utility.JsonObjectBuilder;
import com.zergatul.cheatutils.mcp.utility.Serializer;
import com.zergatul.cheatutils.scripting.workspace.ScriptDocument;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.cheatutils.scripting.workspace.ScriptSlot;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;

import java.io.IOException;
import java.time.Instant;

public class GetLastAttemptedScriptTool implements McpTool {

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
                        "type": "string",
                        "description": "Last code passed to save_script or the web UI"
                    },
                    "diagnostics": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "properties": {
                                "message": {
                                    "type": "string",
                                    "description": "Compiler diagnostic"
                                },
                                "range": {
                                    "type": "object",
                                    "properties": {
                                        "line1": {
                                            "type": "number"
                                        },
                                        "column1": {
                                            "type": "number"
                                        },
                                        "line2": {
                                            "type": "number"
                                        },
                                        "column2": {
                                            "type": "number"
                                        }
                                    },
                                    "required": ["line1", "column1", "line2", "column2"],
                                    "additionalProperties": false
                                }
                            },
                            "required": ["message", "range"],
                            "additionalProperties": false
                        }
                    },
                    "at": {
                        "type": "string",
                        "description": "ISO time of last attempt"
                    },
                    "now": {
                        "type": "string",
                        "description": "Current ISO time"
                    }
                },
                "required": ["now"],
                "additionalProperties": false
            }
            """).getAsJsonObject();

    @Override
    public String getName() {
        return "get_last_attempted_script";
    }

    @Override
    public String getTitle() {
        return "Get Last Attempted Script";
    }

    @Override
    public String getDescription() {
        return "Get last attempted script code and diagnostics, including failed saves.";
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
        ScriptRef ref = new Gson().fromJson(arguments, ScriptRef.class);
        ScriptSlot descriptor = ScriptWorkspace.INSTANCE.get(ref.type());
        ScriptDocument instance = descriptor.getInstance(ref.identifier());
        JsonObject result = new JsonObjectBuilder()
                .withOptionalProperty("code", instance.lastAttemptCode)
                .withOptionalProperty("diagnostics", Serializer.serialize(instance.lastAttemptDiagnostics))
                .withOptionalProperty("at", instance.lastAttemptAt != null ? instance.lastAttemptAt.toString() : null)
                .withProperty("now", Instant.now().toString())
                .build();
        return McpToolCallResult.success(result);
    }
}