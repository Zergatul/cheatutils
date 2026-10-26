package com.zergatul.cheatutils.modules.scripting;

import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.McpServerConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class McpServer implements Module {

    public static final McpServer INSTANCE = new McpServer();

    private final SettingHandle2<McpServerConfig> settings = Settings.get(McpServerConfig.class);

    private McpServer() {
        WebApiRegistry.INSTANCE.register("mcp-server", settings, McpServerConfig.class);
    }

    public SettingHandle2<McpServerConfig> getSettings() {
        return settings;
    }
}