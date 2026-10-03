package com.zergatul.cheatutils.modules.scripting;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.McpServerConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class McpServer implements Module {

    public static final McpServer INSTANCE = new McpServer();

    private McpServer() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<McpServerConfig> {

        public WebApi() {
            super("mcp-server", McpServerConfig.class);
        }

        @Override
        protected McpServerConfig getConfig() {
            return ConfigStore.instance.getConfig().mcpServerConfig;
        }

        @Override
        protected void setConfig(McpServerConfig config) {
            ConfigStore.instance.getConfig().mcpServerConfig = config;
        }
    }
}