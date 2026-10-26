package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.UserNameConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;

public class UserName implements Module {

    public static final UserName INSTANCE = new UserName();

    private final SettingHandle2<UserNameConfig> settings = Settings.get(UserNameConfig.class);

    private UserName() {
        settings.onChanged(this::onConfigChanged);

        WebApiRegistry.INSTANCE.register("user-name", settings, UserNameConfig.class);
    }

    public SettingHandle2<UserNameConfig> getSettings() {
        return settings;
    }

    private void onConfigChanged(UserNameConfig prev, UserNameConfig curr) {
        ClientTickEndExecutor.instance.execute(() -> {
            Minecraft.getInstance().updateTitle();
        });
    }
}