package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

public interface McpTool {

    /**
     * Intended for programmatic or logical use, but used as a display name in past specs or fallback
     * (if title isn’t present).
     */
    String getName();

    /**
     * Intended for UI and end-user contexts — optimized to be human-readable and easily understood,
     * even by those unfamiliar with domain-specific terminology.
     */
    @Nullable String getTitle();

    /**
     * <p>A human-readable description of the tool.</p>
     * <p>This can be used by clients to improve the LLM’s understanding of available tools.
     * It can be thought of like a “hint” to the model.</p>
     */
    @Nullable String getDescription();

    /**
     * A JSON Schema object defining the expected parameters for the tool.
     */
    JsonObject getInputSchema();

    /**
     * An optional JSON Schema object defining the structure of the tool’s output returned
     * in the structuredContent field of a CallToolResult.
     */
    JsonObject getOutputSchema();

    JsonObject invoke(JsonElement arguments);
}