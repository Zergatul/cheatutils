package com.zergatul.cheatutils.mcp.protocol;

import org.jspecify.annotations.Nullable;

public record TextResourceContents(String uri, @Nullable String mimeType, String text) implements ResourceContents {}