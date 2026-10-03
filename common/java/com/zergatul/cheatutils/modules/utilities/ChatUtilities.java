package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import com.zergatul.cheatutils.configs.ChatUtilitiesConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;

import java.util.Objects;

public class ChatUtilities implements Module {

    public static final ChatUtilities INSTANCE = new ChatUtilities();

    private ChatUtilities() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    private static final class WebApi extends SimpleModuleConfigWebApi<ChatUtilitiesConfig> {

        public WebApi() {
            super("chat-utilities", ChatUtilitiesConfig.class);
        }

        @Override
        protected ChatUtilitiesConfig getConfig() {
            return ConfigStore.instance.getConfig().chatUtilitiesConfig;
        }

        @Override
        protected void setConfig(ChatUtilitiesConfig config) {
            ChatUtilitiesConfig oldConfig = ConfigStore.instance.getConfig().chatUtilitiesConfig;
            ConfigStore.instance.getConfig().chatUtilitiesConfig = config;

            if (oldConfig.showTime != config.showTime || !Objects.equals(oldConfig.timeFormat, config.timeFormat)) {
                ClientTickEndExecutor.instance.execute(() -> Minecraft.getInstance().gui.hud.getChat().rescaleChat());
            }
        }
    }
}