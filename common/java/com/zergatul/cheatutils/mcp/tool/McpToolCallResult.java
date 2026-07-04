package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class McpToolCallResult {

    private final @Nullable JsonObject structuredContent;
    private final @Nullable String errorMessage;

    private McpToolCallResult(@Nullable JsonObject structuredContent, @Nullable String errorMessage) {
        this.structuredContent = structuredContent;
        this.errorMessage = errorMessage;
    }

    public static McpToolCallResult success(JsonObject structuredContent) {
        return new McpToolCallResult(structuredContent, null);
    }

    public static McpToolCallResult error(String message) {
        return new McpToolCallResult(null, message);
    }

    public boolean isError() {
        return errorMessage != null;
    }

    public String getErrorMessage() {
        return Objects.requireNonNull(errorMessage);
    }

    public JsonObject getStructuredContent() {
        return Objects.requireNonNull(structuredContent);
    }
}