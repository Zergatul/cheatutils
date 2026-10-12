package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ElytraTunnelConfig;
import com.zergatul.cheatutils.configs.ModuleConfigSettingHandle;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class ElytraTunnel implements Module {

    public static final ElytraTunnel INSTANCE = new ElytraTunnel();

    private final ModuleConfigSettingHandle<ElytraTunnelConfig> settings = new ModuleConfigSettingHandle<>(
            () -> ConfigStore.instance.getConfig().elytraTunnelConfig,
            config -> ConfigStore.instance.getConfig().elytraTunnelConfig = config);

    private ElytraTunnel() {
        WebApiRegistry.INSTANCE.register("elytra-tunnel", settings, ElytraTunnelConfig.class);
    }

    public ModuleConfigSettingHandle<ElytraTunnelConfig> getSettings() {
        return settings;
    }
}