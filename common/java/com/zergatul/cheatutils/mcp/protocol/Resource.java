package com.zergatul.cheatutils.mcp.protocol;

import org.jspecify.annotations.Nullable;

public record Resource(
        String uri,
        String name,
        @Nullable String title,
        @Nullable String description,
        @Nullable String mimeType
) {}