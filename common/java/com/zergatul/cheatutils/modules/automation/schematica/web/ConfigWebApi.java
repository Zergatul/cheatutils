package com.zergatul.cheatutils.modules.automation.schematica.web;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.SchematicaConfig;
import com.zergatul.cheatutils.modules.automation.schematica.Schematica;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;

public final class ConfigWebApi extends SimpleModuleConfigWebApi<SchematicaConfig> {

    public ConfigWebApi() {
        super("schematica", SchematicaConfig.class);
    }

    @Override
    protected SchematicaConfig getConfig() {
        return ConfigStore.instance.getConfig().schematicaConfig;
    }

    @Override
    protected void setConfig(SchematicaConfig config) {
        SchematicaConfig oldConfig = getConfig();
        ConfigStore.instance.getConfig().schematicaConfig = config;

        boolean oldBlockRenderingState = oldConfig.enabled && oldConfig.renderBlocks;
        boolean newBlockRenderingState = config.enabled && config.renderBlocks;
        boolean oldShadeBlocksState = oldBlockRenderingState && oldConfig.shadeBlocks;
        boolean newShadeBlocksState = newBlockRenderingState && config.shadeBlocks;
        if (oldBlockRenderingState != newBlockRenderingState || oldShadeBlocksState != newShadeBlocksState) {
            Schematica.instance.onBlockRenderingStateChanged();
        }
    }
}