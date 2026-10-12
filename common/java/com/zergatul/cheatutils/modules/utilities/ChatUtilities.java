package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import com.zergatul.cheatutils.configs.*;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.SimpleModuleConfigWebApi;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;

import java.util.Objects;

public class ChatUtilities implements Module {

    public static final ChatUtilities INSTANCE = new ChatUtilities();

    private final SettingHandle<ChatUtilitiesConfig> settings = new SettingHandle<>(
            () -> ConfigStore.instance.getConfig().chatUtilitiesConfig,
            config -> ConfigStore.instance.getConfig().chatUtilitiesConfig = config,
            this::onConfigChanged);

    private ChatUtilities() {
        WebApiRegistry.INSTANCE.register("chat-utilities", settings, ChatUtilitiesConfig.class);
    }

    public SettingHandle<ChatUtilitiesConfig> getSettings() {
        return settings;
    }

    private void onConfigChanged(ChatUtilitiesConfig prev, ChatUtilitiesConfig curr) {
        ClientTickEndExecutor.instance.execute(() -> Minecraft.getInstance().gui.hud.getChat().rescaleChat());
    }
}