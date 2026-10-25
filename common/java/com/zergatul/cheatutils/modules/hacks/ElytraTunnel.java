package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.ElytraTunnelConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class ElytraTunnel implements Module {

    public static final ElytraTunnel INSTANCE = new ElytraTunnel();

    private final SettingHandle2<ElytraTunnelConfig> settings = Settings.get(ElytraTunnelConfig.class);

    private ElytraTunnel() {
        WebApiRegistry.INSTANCE.register("elytra-tunnel", settings, ElytraTunnelConfig.class);
    }

    public SettingHandle2<ElytraTunnelConfig> getSettings() {
        return settings;
    }
}