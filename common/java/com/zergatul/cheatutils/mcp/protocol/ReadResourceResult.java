package com.zergatul.cheatutils.mcp.protocol;

public record ReadResourceResult(ResourceContents[] contents) {

    public static ReadResourceResult of(ResourceContents content) {
        return new ReadResourceResult(new ResourceContents[] { content });
    }
}