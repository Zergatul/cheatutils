package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import com.zergatul.cheatutils.configs.*;
import com.zergatul.cheatutils.configs.modules.ChatUtilitiesConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.WeakHashMap;

public class ChatUtilities implements Module {

    public static final ChatUtilities INSTANCE = new ChatUtilities();

    private final SettingHandle2<ChatUtilitiesConfig> settings = Settings.get(ChatUtilitiesConfig.class);
    private final Map<GuiMessage, LocalDateTime> messageTimeMap = new WeakHashMap<>();
    private DateTimeFormatter formatter;

    private ChatUtilities() {
        settings.onChanged(this::onConfigChanged);

        WebApiRegistry.INSTANCE.register("chat-utilities", settings, ChatUtilitiesConfig.class);

        initFormatter(settings.get());
    }

    public SettingHandle2<ChatUtilitiesConfig> getSettings() {
        return settings;
    }

    public void onGuiMessageCreated(GuiMessage message) {
        messageTimeMap.put(message, LocalDateTime.now());
    }

    public GuiMessage onGuiMessageUpdate(GuiMessage message) {
        LocalDateTime time = messageTimeMap.get(message);
        if (time == null) {
            return message;
        }

        ChatUtilitiesConfig config = ChatUtilities.INSTANCE.getSettings().get();
        if (config.showTime() && formatter != null) {
            Component withTime = Component.literal("") // to keep Style.EMPTY for chat message
                    .append(Component.literal(time.format(formatter)).withStyle(Style.EMPTY.withColor(0xFF808080)))
                    .append(Component.literal(" "))
                    .append(message.content());
            return new GuiMessage(message.addedTime(), withTime, message.signature(), message.source(), message.tag());
        } else {
            return message;
        }
    }

    private void onConfigChanged(ChatUtilitiesConfig prev, ChatUtilitiesConfig curr) {
        ClientTickEndExecutor.instance.execute(() -> {
            initFormatter(curr);
            Minecraft.getInstance().gui.hud.getChat().rescaleChat();
        });
    }

    private void initFormatter(ChatUtilitiesConfig config) {
        if (config.showTime()) {
            formatter = DateTimeFormatter.ofPattern(config.timeFormat());
        } else {
            formatter = null;
        }
    }
}