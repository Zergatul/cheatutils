package com.zergatul.cheatutils.mcp.protocol;

import org.jspecify.annotations.Nullable;

public record Implementation(String name, @Nullable String title, String version) {
    public Implementation(String name, String version) {
        this(name, null, version);
    }
}