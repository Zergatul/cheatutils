package com.zergatul.cheatutils.mcp;

import com.zergatul.cheatutils.configs.McpServerConfig;

public interface McpItem {
    default McpItemStatus getStatus(McpServerConfig config) {
        if (!config.enabled) {
            return McpItemStatus.disabled("MCP Server is disabled in CheatUtils settings. Tool calls/resource reads are not allowed.");
        }
        return McpItemStatus.enabled();
    }
}