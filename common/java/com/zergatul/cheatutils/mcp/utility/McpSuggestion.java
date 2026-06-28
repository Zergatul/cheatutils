package com.zergatul.cheatutils.mcp.utility;

import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

public record McpSuggestion(
        String name,
        String kind,
        @Nullable String type,
        @Nullable String signature,
        @Nullable String documentation
) {
    public JsonObject toJson() {
        return new JsonObjectBuilder()
                .withProperty("name", name)
                .withProperty("kind", kind)
                .withOptionalProperty("type", type)
                .withOptionalProperty("signature", signature)
                .withOptionalProperty("documentation", documentation)
                .build();
    }
}