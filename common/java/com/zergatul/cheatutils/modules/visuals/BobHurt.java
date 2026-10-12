package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.BobHurtConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ModuleConfigSettingHandle;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class BobHurt implements Module {

    public static final BobHurt INSTANCE = new BobHurt();

    private final ModuleConfigSettingHandle<BobHurtConfig> settings = new ModuleConfigSettingHandle<>(
            () -> ConfigStore.instance.getConfig().bobHurtConfig,
            config -> ConfigStore.instance.getConfig().bobHurtConfig = config);

    private BobHurt() {
        WebApiRegistry.INSTANCE.register("bob-hurt", settings, BobHurtConfig.class);
    }

    public ModuleConfigSettingHandle<BobHurtConfig> getSettings() {
        return settings;
    }
}