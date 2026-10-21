package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.configs.modules.EntityTitleConfig;

/** Typed settings catalog. Only this class knows where module values live in the root config. */
public final class Settings {

    private final SettingHandle2<EntityTitleConfig> entityTitle;

    Settings(SettingsRegistry2<Config> registry) {
        entityTitle = registry.register(
                config -> config.entityTitleConfig,
                (config, value) -> config.entityTitleConfig = value);
    }

    public static SettingHandle2<EntityTitleConfig> entityTitle() {
        return ConfigStore.instance.getSettings().entityTitle;
    }
}