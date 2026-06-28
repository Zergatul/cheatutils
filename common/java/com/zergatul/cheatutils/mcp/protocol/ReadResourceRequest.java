package com.zergatul.cheatutils.mcp.protocol;

/**
 * @param uri The URI of the resource. The URI can use any protocol; it is up to the server how to interpret it.
 */
public record ReadResourceRequest(String uri) {}