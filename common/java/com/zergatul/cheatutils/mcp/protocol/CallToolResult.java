package com.zergatul.cheatutils.mcp.protocol;

import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

public record CallToolResult(
        ContentBlock[] content,
        JsonObject structuredContent,
        @Nullable Boolean isError
) {}