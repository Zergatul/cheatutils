package com.zergatul.cheatutils.mcp.protocol;

public record InitializeResult(String protocolVersion, Implementation serverInfo, ServerCapabilities capabilities) {}