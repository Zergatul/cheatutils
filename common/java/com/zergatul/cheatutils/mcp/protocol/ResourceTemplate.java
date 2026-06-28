package com.zergatul.cheatutils.mcp.protocol;

import org.jspecify.annotations.Nullable;

public record ResourceTemplate(
        String uriTemplate,
        String name,
        @Nullable String title,
        @Nullable String description,
        @Nullable String mimeType
) {}