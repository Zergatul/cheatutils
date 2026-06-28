package com.zergatul.cheatutils.mcp.protocol;

import org.jspecify.annotations.Nullable;

public record CallToolResult(ContentBlock[] content, @Nullable Boolean isError) {

    public static CallToolResult ofText(String text) {
        return new CallToolResult(new ContentBlock[] { new TextContent(text) }, null);
    }
}