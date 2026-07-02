package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zergatul.cheatutils.mcp.utility.JsonArrayBuilder;
import com.zergatul.cheatutils.mcp.utility.JsonObjectBuilder;
import com.zergatul.cheatutils.scripting.services.*;

import java.io.IOException;

public class SaveScriptTool implements McpTool {

    private final JsonObject inputSchema = JsonParser.parseString("""
            {
                "type": "object",
                "properties": {
                    "locator": {
                        "type": "object",
                        "properties": {
                            "type": {
                                "type": "string",
                                "description": "Script type"
                            },
                            "identifier": {
                                "type": "string",
                                "description": "Script identifier",
                            }
                        },
                        "required": ["type"],
                        "additionalProperties": false
                    },
                    "code": {
                        "type": "string"
                    }
                },
                "required": ["locator", "code"],
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
                                    "type": "string"
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
                        "type": "boolean"
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
        return "Compiles and saves script, updating corresponding module live.";
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
        ScriptLocator locator = new Gson().fromJson(arguments.getAsJsonObject().getAsJsonObject("locator"), ScriptLocator.class);
        String code = arguments.getAsJsonObject().getAsJsonPrimitive("code").getAsString();
        ScriptStorageDescriptor descriptor = ScriptWorkspaceService.INSTANCE.get(locator.type());
        ScriptSaveResult result = descriptor.save(locator.identifier(), code);

        JsonObject output = new JsonObject();
        output.addProperty("ok", result.isSuccess());
        if (!result.isSuccess()) {
            output.add("diagnostics", new JsonArrayBuilder()
                    .withItems(result.getDiagnostics().stream().map(diagnostic -> new JsonObjectBuilder()
                            .withProperty("message", diagnostic.message)
                            .withProperty("range", new JsonObjectBuilder()
                                    .withProperty("line1", diagnostic.range.getLine1())
                                    .withProperty("column1", diagnostic.range.getColumn1())
                                    .withProperty("line2", diagnostic.range.getLine2())
                                    .withProperty("column2", diagnostic.range.getColumn2())
                                    .build())
                            .build()))
                    .build());
        }

        return output;
    }
}