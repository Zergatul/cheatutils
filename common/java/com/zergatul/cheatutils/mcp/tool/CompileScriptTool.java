package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zergatul.cheatutils.mcp.utility.Serializer;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptCompileResult;
import com.zergatul.cheatutils.scripting.workspace.ScriptSlot;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;

import java.io.IOException;

public class CompileScriptTool implements McpTool {

    private final JsonObject inputSchema = JsonParser.parseString("""
            {
                "type": "object",
                "properties": {
                    "type": {
                        "type": "string",
                        "description": "ScriptType enum name"
                    },
                    "code": {
                        "type": "string",
                        "description": "Script source to check"
                    }
                },
                "required": ["type", "code"],
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
                        "description": "True when compilation succeeds"
                    }
                },
                "required": ["ok"],
                "additionalProperties": false
            }
            """).getAsJsonObject();

    @Override
    public String getName() {
        return "compile_script";
    }

    @Override
    public String getTitle() {
        return "Compile Script";
    }

    @Override
    public String getDescription() {
        return "Compile script without saving.";
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
        ScriptType type = ScriptType.valueOf(arguments.getAsJsonObject().getAsJsonPrimitive("type").getAsString());
        String code = arguments.getAsJsonObject().getAsJsonPrimitive("code").getAsString();

        ScriptSlot descriptor = ScriptWorkspace.INSTANCE.get(type);
        ScriptCompileResult result = descriptor.compile(code);

        JsonObject output = new JsonObject();
        output.addProperty("ok", result.isSuccess());
        if (!result.isSuccess()) {
            output.add("diagnostics", Serializer.serialize(result.getDiagnostics()));
        }

        return output;
    }
}