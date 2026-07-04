package com.zergatul.cheatutils.mcp.resource;

import com.zergatul.cheatutils.mcp.protocol.ResourceContents;
import com.zergatul.cheatutils.mcp.protocol.TextResourceContents;
import com.zergatul.cheatutils.mcp.utility.URITemplate;
import com.zergatul.cheatutils.utils.ResourceHelper;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public class ScriptTypeDocumentationResource implements McpResourceTemplate {

    private final URITemplate template = URITemplate.parse("cheatutils://docs/script-type/{type}");

    @Override
    public URITemplate getUriTemplate() {
        return template;
    }

    @Override
    public String getName() {
        return "script_type";
    }

    @Override
    public String getTitle() {
        return "Script Type Documentation";
    }

    @Override
    public String getDescription() {
        return "Purpose, execution model, rules, and examples for a particular script type.";
    }

    @Override
    public @Nullable String getMimeType() {
        return "text/markdown";
    }

    @Override
    public boolean hasResource(String uri) {
        Optional<Map<String, String>> optional = template.match(uri);
        if (optional.isEmpty()) {
            return false;
        }

        String type = optional.get().get("type");
        return ResourceHelper.has("llm/script-types/" + type + ".md");
    }

    @Override
    public ResourceContents getContent(String uri) throws IOException {
        Optional<Map<String, String>> optional = template.match(uri);
        if (optional.isEmpty()) {
            throw new IllegalStateException();
        }

        String type = optional.get().get("type");
        InputStream stream = ResourceHelper.get("llm/script-types/" + type + ".md");
        if (stream == null) {
            throw new IllegalStateException();
        }

        try (stream) {
            byte[] raw = org.apache.commons.io.IOUtils.toByteArray(stream);
            String content = new String(raw, StandardCharsets.UTF_8);
            return new TextResourceContents(uri, getMimeType(), content);
        }
    }
}