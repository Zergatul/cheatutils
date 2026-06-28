package com.zergatul.cheatutils.mcp.protocol;

public record ImageContent(String type, String data, String mimeType) implements ContentBlock {

    public ImageContent(String data, String mimeType) {
        this("image", data, mimeType);
    }
}