package com.zergatul.cheatutils.mcp.resource;

import com.zergatul.cheatutils.mcp.utility.URITemplate;
import com.zergatul.cheatutils.mcp.protocol.ResourceContents;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public interface McpResourceTemplate {

    /**
     * A URI template (according to RFC 6570) that can be used to construct resource URIs.
     */
    URITemplate getUriTemplate();

    /**
     * Intended for programmatic or logical use, but used as a display name in past specs or fallback
     * (if title isn’t present).
     */
    String getName();

    /**
     * Intended for UI and end-user contexts — optimized to be human-readable and easily understood,
     * even by those unfamiliar with domain-specific terminology.
     */
    @Nullable String getTitle();

    /**
     * A description of what this resource represents.
     * <p>This can be used by clients to improve the LLM’s understanding of available resources.
     * It can be thought of like a “hint” to the model.</p>
     */
    @Nullable String getDescription();

    /**
     * The MIME type for all resources that match this template.
     * This should only be included if all resources matching this template have the same type.
     */
    @Nullable String getMimeType();

    boolean hasResource(String uri);

    ResourceContents getContent(String uri) throws IOException;
}