package com.zergatul.cheatutils.mcp.protocol;

import org.jspecify.annotations.Nullable;

public record ServerCapabilities(@Nullable Prompts prompts, @Nullable Resources resources, @Nullable Tools tools) {

    public ServerCapabilities(@Nullable Resources resources, @Nullable Tools tools) {
        this(null, resources, tools);
    }

    public record Prompts(boolean listChanged) {}
    public record Resources(boolean subscribe, boolean listChanged) {}
    public record Tools(boolean listChanged) {}
}