package com.zergatul.cheatutils.mcp.protocol;

public record TextContent(String type, String text) implements ContentBlock {

    public TextContent(String text) {
        this("text", text);
    }
}