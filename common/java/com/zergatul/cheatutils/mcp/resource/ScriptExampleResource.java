package com.zergatul.cheatutils.mcp.resource;

import com.zergatul.cheatutils.mcp.protocol.ResourceContents;
import com.zergatul.cheatutils.mcp.protocol.TextResourceContents;
import com.zergatul.cheatutils.mcp.utility.URITemplate;
import com.zergatul.cheatutils.utils.ResourceHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public class ScriptExampleResource implements McpResourceTemplate {

    private final URITemplate template = URITemplate.parse("cheatutils://docs/examples/{directory}/{file}");

    @Override
    public URITemplate getUriTemplate() {
        return template;
    }

    @Override
    public String getName() {
        return "script_example";
    }

    @Override
    public String getTitle() {
        return "Script Example";
    }

    @Override
    public String getDescription() {
        return "Source code of example script";
    }

    @Override
    public String getMimeType() {
        return "text/plain";
    }

    @Override
    public boolean hasResource(String uri) {
        return ResourceHelper.has(uriToResourcePath(uri));
    }

    @Override
    public ResourceContents getContent(String uri) throws IOException {
        InputStream stream = ResourceHelper.get(uriToResourcePath(uri));
        if (stream == null) {
            throw new IllegalStateException();
        }

        try (stream) {
            String content = new InputStreamReader(stream, StandardCharsets.UTF_8).readAllAsString();
            return new TextResourceContents(uri, getMimeType(), content);
        }
    }

    private String uriToResourcePath(String uri) {
        Optional<Map<String, String>> optional = template.match(uri);
        if (optional.isEmpty()) {
            throw new IllegalStateException();
        }

        Map<String, String> variables = optional.get();
        String directory = variables.get("directory");
        String file = variables.get("file");
        if (directory == null || file == null) {
            throw new IllegalStateException();
        }

        return "llm/script-examples/" + directory + "/" + file + ".cs";
    }
}