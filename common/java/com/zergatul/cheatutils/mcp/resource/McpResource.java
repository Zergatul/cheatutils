package com.zergatul.cheatutils.mcp.resource;

import org.jspecify.annotations.Nullable;

public interface McpResource {

    /**
     * The URI of this resource.
     */
    String getUri();

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
     * The MIME type of this resource, if known.
     */
    @Nullable String getMimeType();
}