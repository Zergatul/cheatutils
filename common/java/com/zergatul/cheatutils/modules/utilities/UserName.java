package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.configs.UserNameConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;

public class UserName implements Module {

    public static final UserName INSTANCE = new UserName();

    private final SettingHandle<UserNameConfig> settings = new SettingHandle<>(
            () -> ConfigStore.instance.getConfig().userNameConfig,
            config -> ConfigStore.instance.getConfig().userNameConfig = config,
            this::onConfigChanged);

    private UserName() {
        WebApiRegistry.INSTANCE.register("user-name", settings, UserNameConfig.class);
    }

    public SettingHandle<UserNameConfig> getSettings() {
        return settings;
    }

    private void onConfigChanged(UserNameConfig prev, UserNameConfig curr) {
        ClientTickEndExecutor.instance.execute(() -> {
            Minecraft.getInstance().updateTitle();
        });
    }
}