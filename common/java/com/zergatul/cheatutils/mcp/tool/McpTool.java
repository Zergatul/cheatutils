package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

public interface McpTool {
    String getName();
    @Nullable String getTitle();
    @Nullable String getDescription();
    JsonObject getInputSchema();
    JsonObject getOutputSchema();
    JsonObject invoke(JsonElement arguments);
}