package com.zergatul.cheatutils.modules.scripting;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.McpServerConfig;
import com.zergatul.cheatutils.configs.ModuleConfigSettingHandle;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class McpServer implements Module {

    public static final McpServer INSTANCE = new McpServer();

    private final ModuleConfigSettingHandle<McpServerConfig> settings = new ModuleConfigSettingHandle<>(
            () -> ConfigStore.instance.getConfig().mcpServerConfig,
            config -> ConfigStore.instance.getConfig().mcpServerConfig = config);

    private McpServer() {
        WebApiRegistry.INSTANCE.register("mcp-server", settings, McpServerConfig.class);
    }

    public ModuleConfigSettingHandle<McpServerConfig> getSettings() {
        return settings;
    }
}