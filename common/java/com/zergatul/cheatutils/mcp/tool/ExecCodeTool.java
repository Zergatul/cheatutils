package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zergatul.cheatutils.configs.McpServerConfig;
import com.zergatul.cheatutils.mcp.McpItemStatus;
import com.zergatul.cheatutils.mcp.utility.JsonObjectBuilder;
import com.zergatul.cheatutils.mcp.utility.Serializer;
import com.zergatul.cheatutils.scripting.Root;
import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.scripting.compiler.CompilationResult;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ExecCodeTool implements McpTool {

    private final JsonObject inputSchema = JsonParser.parseString("""
            {
                "type": "object",
                "properties": {
                    "code": {
                        "type": "string",
                        "description": "CheatUtils scripting code to compile and run"
                    }
                },
                "required": ["code"],
                "additionalProperties": false
            }
            """).getAsJsonObject();

    private final JsonObject outputSchema = JsonParser.parseString("""
            {
                "type": "object",
                "properties": {
                    "ok": {
                        "type": "boolean",
                        "description": "True when compilation and execution both succeed"
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
                    "records": {
                        "type": "array",
                        "description": "Messages written through executionLog.write()",
                        "items": {
                            "type": "string"
                        }
                    },
                    "error": {
                        "type": "string",
                        "description": "Runtime error message"
                    }
                },
                "required": ["ok"],
                "additionalProperties": false
            }
            """).getAsJsonObject();

    @Override
    public String getName() {
        return "exec_code";
    }

    @Override
    public String getTitle() {
        return "Execute Code";
    }

    @Override
    public String getDescription() {
        return
                "Compile and run CheatUtils scripting code using the Exec script context. " +
                "Execution runs on the main thread, can mutate game or mod state, and has no timeout. " +
                "Use executionLog.write(message) in the script to return records.";
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

        if (!config.allowExecutingCode) {
            return McpItemStatus.disabled("Disabled by CheatUtils MCP Server settings: allowExecutingCode is false.");
        }

        return McpItemStatus.enabled();
    }

    @Override
    public McpToolCallResult invoke(JsonElement arguments) throws IOException {
        if (arguments == null || !arguments.isJsonObject()) {
            return McpToolCallResult.error("Invalid arguments: expected object with string property 'code'.");
        }

        JsonElement codeElement = arguments.getAsJsonObject().get("code");
        if (codeElement == null || !codeElement.isJsonPrimitive() || !codeElement.getAsJsonPrimitive().isString()) {
            return McpToolCallResult.error("Invalid arguments: expected string property 'code'.");
        }

        CompilationResult compileResult = ScriptCompilerRegistry.INSTANCE.compile(ScriptType.EXEC_CODE, codeElement.getAsString());
        Runnable program = compileResult.getProgram();
        if (program == null) {
            return McpToolCallResult.success(new JsonObjectBuilder()
                    .withProperty("ok", false)
                    .withProperty("diagnostics", Serializer.serialize(compileResult.getDiagnostics()))
                    .build());
        }

        ExecutionResult result;
        try {
            result = execute(program);
        } catch (IllegalStateException e) {
            return McpToolCallResult.error(e.getMessage());
        }

        JsonObjectBuilder builder = new JsonObjectBuilder()
                .withProperty("ok", result.ok())
                .withProperty("records", serializeRecords(result.records()));
        if (result.error() != null) {
            builder.withProperty("error", result.error());
        }

        return McpToolCallResult.success(builder.build());
    }

    private ExecutionResult execute(Runnable program) {
        CompletableFuture<ExecutionResult> future = new CompletableFuture<>();

        try {
            Minecraft.getInstance().execute(() -> {
                Root.executionLog.clear();
                try {
                    program.run();
                    future.complete(new ExecutionResult(true, Root.executionLog.getRecords(), null));
                } catch (Throwable e) {
                    future.complete(new ExecutionResult(false, Root.executionLog.getRecords(), formatError(e)));
                }
            });
        } catch (RuntimeException e) {
            throw new IllegalStateException("Cannot schedule code execution on main thread.", e);
        }

        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for code execution.", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Code execution failed outside normal scripting error handling.", e.getCause());
        }
    }

    private JsonArray serializeRecords(List<String> records) {
        JsonArray array = new JsonArray();
        for (String record : records) {
            array.add(record);
        }
        return array;
    }

    private String formatError(Throwable e) {
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            return e.getClass().getName();
        } else {
            return e.getClass().getName() + ": " + message;
        }
    }

    private record ExecutionResult(boolean ok, List<String> records, String error) {}
}