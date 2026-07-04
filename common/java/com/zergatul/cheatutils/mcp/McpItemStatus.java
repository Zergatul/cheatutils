package com.zergatul.cheatutils.mcp;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@NullMarked
public class McpItemStatus {

    private final boolean enabled;
    private final @Nullable String message;

    private McpItemStatus(boolean enabled, @Nullable String message) {
        this.enabled = enabled;
        this.message = message;
    }

    public static McpItemStatus enabled() {
        return new McpItemStatus(true, null);
    }

    public static McpItemStatus disabled(String message) {
        return new McpItemStatus(false, message);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getMessage() {
        return Objects.requireNonNull(message);
    }
}