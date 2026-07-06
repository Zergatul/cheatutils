package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zergatul.cheatutils.configs.McpServerConfig;
import com.zergatul.cheatutils.mcp.McpItemStatus;
import com.zergatul.cheatutils.mcp.utility.JsonObjectBuilder;
import com.zergatul.cheatutils.mcp.utility.Serializer;
import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.scripting.compiler.ExpressionCompilationResult;
import com.zergatul.scripting.runtime.ExpressionEvaluationResult;
import com.zergatul.scripting.runtime.ExpressionEvaluator;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class EvaluateExpressionTool implements McpTool {

    private final JsonObject inputSchema = JsonParser.parseString("""
            {
                "type": "object",
                "properties": {
                    "expression": {
                        "type": "string",
                        "description": "CheatUtils scripting expression to compile and evaluate"
                    }
                },
                "required": ["expression"],
                "additionalProperties": false
            }
            """).getAsJsonObject();

    private final JsonObject outputSchema = JsonParser.parseString("""
            {
                "type": "object",
                "oneOf": [
                    {
                        "type": "object",
                        "properties": {
                            "ok": {
                                "const": false
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
                            }
                        },
                        "required": ["ok", "diagnostics"],
                        "additionalProperties": false
                    },
                    {
                        "type": "object",
                        "properties": {
                            "ok": {
                                "type": "boolean"
                            },
                            "hasValue": {
                                "type": "boolean"
                            },
                            "type": {
                                "type": "string",
                                "description": "Scripting language type"
                            },
                            "javaType": {
                                "type": "string",
                                "description": "Java canonical class name"
                            },
                            "value": {
                                "type": "string",
                                "description": "Expression evaluation result as string"
                            }
                        },
                        "required": ["ok", "hasValue", "type", "javaType", "value"],
                        "additionalProperties": false
                    }
                ]
            }
            """).getAsJsonObject();

    @Override
    public String getName() {
        return "evaluate_expression";
    }

    @Override
    public String getTitle() {
        return "Evaluate Expression";
    }

    @Override
    public String getDescription() {
        return
                "Compile and evaluate a CheatUtils scripting expression using the Eval script context. " +
                "Evaluation runs on the main thread, can mutate game or mod state, and has no timeout.";
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

        if (!config.allowEvaluatingExpressions) {
            return McpItemStatus.disabled("Disabled by CheatUtils MCP Server settings: allowEvaluatingExpressions is false.");
        }

        return McpItemStatus.enabled();
    }

    @Override
    public McpToolCallResult invoke(JsonElement arguments) throws IOException {
        if (arguments == null || !arguments.isJsonObject()) {
            return McpToolCallResult.error("Invalid arguments: expected object with string property 'expression'.");
        }

        JsonElement expressionElement = arguments.getAsJsonObject().get("expression");
        if (expressionElement == null || !expressionElement.isJsonPrimitive() || !expressionElement.getAsJsonPrimitive().isString()) {
            return McpToolCallResult.error("Invalid arguments: expected string property 'expression'.");
        }

        String expression = expressionElement.getAsString();
        ExpressionCompilationResult compileResult = ScriptCompilerRegistry.INSTANCE.compileAsExpression(ScriptType.EXPR_EVAL, expression);
        if (!compileResult.isSuccessful()) {
            return McpToolCallResult.success(new JsonObjectBuilder()
                    .withProperty("ok", false)
                    .withProperty("diagnostics", Serializer.serialize(compileResult.getDiagnostics()))
                    .build());
        }

        ExpressionEvaluationResult result;
        try {
            result = evaluate(compileResult.getProgram());
        } catch (IllegalStateException e) {
            return McpToolCallResult.error(e.getMessage());
        }

        return McpToolCallResult.success(new JsonObjectBuilder()
                .withProperty("ok", result.ok())
                .withProperty("hasValue", result.hasValue())
                .withProperty("type", result.type())
                .withProperty("javaType", result.javaType())
                .withProperty("value", result.value())
                .build());
    }

    private ExpressionEvaluationResult evaluate(ExpressionEvaluator program) {
        CompletableFuture<ExpressionEvaluationResult> future = new CompletableFuture<>();

        try {
            Minecraft.getInstance().execute(() -> {
                try {
                    future.complete(program.evaluate());
                } catch (Throwable e) {
                    future.completeExceptionally(e);
                }
            });
        } catch (RuntimeException e) {
            throw new IllegalStateException("Cannot schedule expression evaluation on main thread.", e);
        }

        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for expression evaluation.", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Expression evaluation failed outside normal scripting error handling.", e.getCause());
        }
    }
}