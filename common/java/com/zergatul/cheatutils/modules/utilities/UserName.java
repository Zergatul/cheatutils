package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.UserNameConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;

public class UserName implements Module {

    public static final UserName INSTANCE = new UserName();

    private UserName() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<UserNameConfig> {

        public WebApi() {
            super("user-name", UserNameConfig.class);
        }

        @Override
        protected UserNameConfig getConfig() {
            return ConfigStore.instance.getConfig().userNameConfig;
        }

        @Override
        protected void setConfig(UserNameConfig config) {
            ConfigStore.instance.getConfig().userNameConfig = config;
            Minecraft.getInstance().updateTitle();
        }
    }
}