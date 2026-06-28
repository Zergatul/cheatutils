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
        return "Example script source.";
    }

    @Override
    public String getMimeType() {
        return "text/plain";
    }

    @Override
    public boolean hasResource(String uri) {
        Optional<String> path = tryUriToResourcePath(uri);
        return path.isPresent() && ResourceHelper.has(path.get());
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
        return tryUriToResourcePath(uri).orElseThrow(IllegalStateException::new);
    }

    private Optional<String> tryUriToResourcePath(String uri) {
        Optional<Map<String, String>> optional = template.match(uri);
        if (optional.isEmpty()) {
            return Optional.empty();
        }

        Map<String, String> variables = optional.get();
        String directory = variables.get("directory");
        String file = variables.get("file");
        if (directory == null || file == null) {
            return Optional.empty();
        }

        return Optional.of("llm/script-examples/" + directory + "/" + file + ".cs");
    }
}