package com.zergatul.cheatutils.mcp.resource;

import com.zergatul.cheatutils.mcp.protocol.ResourceContents;
import com.zergatul.cheatutils.mcp.utility.URITemplate;
import com.zergatul.cheatutils.utils.ResourceHelper;
import org.jspecify.annotations.Nullable;

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
        return "Purpose, example use cases, tricks, and other information about particular script type.";
    }

    @Override
    public @Nullable String getMimeType() {
        return "text/plain";
    }

    @Override
    public boolean hasResource(String uri) {
        Optional<Map<String, String>> optional = template.match(uri);
        if (optional.isEmpty()) {
            return false;
        }

        String type = optional.get().get("type");
        return ResourceHelper.has("llm/script-types/" + type);
    }

    @Override
    public ResourceContents getContent(String uri) {
        return null;
    }
}