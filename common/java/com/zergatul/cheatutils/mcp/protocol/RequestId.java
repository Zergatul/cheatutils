package com.zergatul.cheatutils.mcp.protocol;

import org.jspecify.annotations.Nullable;

public record RequestId(@Nullable String strValue, @Nullable Integer numValue) {

    public RequestId(String value) {
        this(value, null);
    }

    public RequestId(int value) {
        this(null, value);
    }
}