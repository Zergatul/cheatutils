package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public interface McpTool {
    String getName();
    String getTitle();
    String getDescription();
    JsonObject getInputSchema();
    JsonObject getOutputSchema();
    JsonElement invoke(JsonElement arguments);
}