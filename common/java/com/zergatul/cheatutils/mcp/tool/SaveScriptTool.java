package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zergatul.cheatutils.configs.McpServerConfig;
import com.zergatul.cheatutils.mcp.McpItem;
import com.zergatul.cheatutils.mcp.McpItemStatus;
import com.zergatul.cheatutils.mcp.utility.Serializer;
import com.zergatul.cheatutils.scripting.workspace.*;

import java.io.IOException;

public class SaveScriptTool implements McpTool {

    private final JsonObject inputSchema = JsonParser.parseString("""
            {
                "type": "object",
                "properties": {
                    "ref": {
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
                    },
                    "code": {
                        "type": "string",
                        "description": "Script source to compile and save"
                    }
                },
                "required": ["ref", "code"],
                "additionalProperties": false
            }
            """).getAsJsonObject();

    private final JsonObject outputSchema = JsonParser.parseString("""
            {
                "type": "object",
                "properties": {
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
                    "ok": {
                        "type": "boolean",
                        "description": "True when save succeeds"
                    }
                },
                "required": ["ok"],
                "additionalProperties": false
            }
            """).getAsJsonObject();

    @Override
    public String getName() {
        return "save_script";
    }

    @Override
    public String getTitle() {
        return "Save Script";
    }

    @Override
    public String getDescription() {
        return "Compile and save script. Applies live if successful.";
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
    public McpItemStatus getStatus(McpServerConfig config) {
        McpItemStatus status = McpTool.super.getStatus(config);
        if (!status.isEnabled()) {
            return status;
        }

        if (!config.allowSavingScripts) {
            return McpItemStatus.disabled("Disabled by CheatUtils MCP Server settings: allowSavingScripts is false.");
        }

        return McpItemStatus.enabled();
    }

    @Override
    public McpToolCallResult invoke(JsonElement arguments) throws IOException {
        ScriptRef ref = new Gson().fromJson(arguments.getAsJsonObject().getAsJsonObject("ref"), ScriptRef.class);
        String code = arguments.getAsJsonObject().getAsJsonPrimitive("code").getAsString();
        ScriptSlot descriptor = ScriptWorkspace.INSTANCE.get(ref.type());
        ScriptSaveResult saveResult = descriptor.save(ref.identifier(), code);

        JsonObject result = new JsonObject();
        result.addProperty("ok", saveResult.isSuccess());
        if (!saveResult.isSuccess()) {
            result.add("diagnostics", Serializer.serialize(saveResult.getDiagnostics()));
        }

        return McpToolCallResult.success(result);
    }
}