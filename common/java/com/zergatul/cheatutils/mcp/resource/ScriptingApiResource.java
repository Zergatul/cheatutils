package com.zergatul.cheatutils.mcp.resource;

import com.zergatul.cheatutils.mcp.protocol.ResourceContents;
import com.zergatul.cheatutils.mcp.protocol.TextResourceContents;
import com.zergatul.cheatutils.scripting.ApiDocsGenerator;

import java.util.List;

public class ScriptingApiResource implements McpResource {

    @Override
    public String getUri() {
        return "cheatutils://docs/api";
    }

    @Override
    public String getName() {
        return "api";
    }

    @Override
    public String getTitle() {
        return "Scripting API";
    }

    @Override
    public String getDescription() {
        return "Auto-generated API reference. Contains list of available classes/methods/properties accessible for scripting.";
    }

    @Override
    public String getMimeType() {
        return "text/plain";
    }

    @Override
    public ResourceContents getContent() {
        List<String> lines = ApiDocsGenerator.generateLines(false);

        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append(line);
            builder.append('\n');
        }

        return new TextResourceContents(getUri(), getMimeType(), builder.toString());
    }
}