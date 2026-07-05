package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.JsonObject;
import com.zergatul.cheatutils.mcp.protocol.ContentBlock;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class McpToolCallResult {

    private final @Nullable JsonObject structuredContent;
    private final @Nullable ContentBlock content;
    private final @Nullable String errorMessage;

    private McpToolCallResult(
            @Nullable JsonObject structuredContent,
            @Nullable ContentBlock content,
            @Nullable String errorMessage
    ) {
        this.structuredContent = structuredContent;
        this.content = content;
        this.errorMessage = errorMessage;
    }

    public static McpToolCallResult success(JsonObject structuredContent) {
        return new McpToolCallResult(structuredContent, null, null);
    }

    public static McpToolCallResult success(ContentBlock content) {
        return new McpToolCallResult(null, content, null);
    }

    public static McpToolCallResult error(String message) {
        return new McpToolCallResult(null, null, message);
    }

    public boolean isError() {
        return errorMessage != null;
    }

    public boolean isStructured() {
        return structuredContent != null;
    }

    public String getErrorMessage() {
        return Objects.requireNonNull(errorMessage);
    }

    public JsonObject getStructuredContent() {
        return Objects.requireNonNull(structuredContent);
    }

    public ContentBlock getContent() {
        return Objects.requireNonNull(content);
    }
}