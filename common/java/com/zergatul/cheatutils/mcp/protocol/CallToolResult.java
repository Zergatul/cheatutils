package com.zergatul.cheatutils.mcp.protocol;

import org.jspecify.annotations.Nullable;

public record CallToolResult(ContentBlock[] content, @Nullable Boolean isError) {
}