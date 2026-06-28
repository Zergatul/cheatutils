package com.zergatul.cheatutils.mcp.protocol;

import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

public record Tool(
        String name,
        @Nullable String title,
        @Nullable String description,
        JsonObject inputSchema,
        @Nullable JsonObject outputSchema
) {}