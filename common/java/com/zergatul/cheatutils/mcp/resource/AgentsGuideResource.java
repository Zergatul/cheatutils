package com.zergatul.cheatutils.mcp.resource;

import com.zergatul.cheatutils.mcp.protocol.ResourceContents;
import com.zergatul.cheatutils.mcp.protocol.TextResourceContents;
import com.zergatul.cheatutils.utils.ResourceHelper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AgentsGuideResource implements McpResource {

    @Override
    public String getUri() {
        return "cheatutils://docs/agents-guide";
    }

    @Override
    public String getName() {
        return "agents_guide";
    }

    @Override
    public String getTitle() {
        return "Agents Guide";
    }

    @Override
    public String getDescription() {
        return "General guidelines for coding agents about CheatUtils scripting";
    }

    @Override
    public String getMimeType() {
        return "text/markdown";
    }

    @Override
    public ResourceContents getContent() throws IOException {
        InputStream stream = ResourceHelper.get("llm/agents-guide.md");
        if (stream == null) {
            throw new IOException("Resource not found");
        }

        try (stream) {
            byte[] raw = org.apache.commons.io.IOUtils.toByteArray(stream);
            String content = new String(raw, StandardCharsets.UTF_8);
            return new TextResourceContents(getUri(), getMimeType(), content);
        }
    }
}