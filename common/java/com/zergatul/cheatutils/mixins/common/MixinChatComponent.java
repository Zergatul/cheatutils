package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ChatUtilitiesConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.WeakHashMap;

@Mixin(ChatComponent.class)
public abstract class MixinChatComponent {

    @Unique
    private final Map<GuiMessage, LocalDateTime> messageTimeMap = new WeakHashMap<>();

    @ModifyConstant(
            method = "addMessageToQueue",
            constant = @Constant(intValue = 100))
    private int addMessageToQueueModifyMaxChatHistory(int size) {
        ChatUtilitiesConfig config = ConfigStore.instance.getConfig().chatUtilitiesConfig;
        return config.overrideMessageLimit ? config.messageLimit : size;
    }

    @ModifyConstant(
            method = "addMessageToDisplayQueue",
            constant = @Constant(intValue = 100))
    private int addMessageToDisplayQueueModifyMaxChatHistory(int size) {
        ChatUtilitiesConfig config = ConfigStore.instance.getConfig().chatUtilitiesConfig;
        return config.overrideMessageLimit ? config.messageLimit : size;
    }

    @ModifyVariable(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
            at = @At(value = "STORE"),
            name = "message")
    private GuiMessage onGuiMessageCreated(GuiMessage message) {
        messageTimeMap.put(message, LocalDateTime.now());
        return message;
    }

    @ModifyVariable(
            at = @At("HEAD"),
            method = "addMessageToDisplayQueue",
            argsOnly = true)
    private GuiMessage onModifyMessageBeforeWrapping(GuiMessage message) {
        LocalDateTime time = messageTimeMap.get(message);
        if (time == null) {
            return message;
        }

        ChatUtilitiesConfig config = ConfigStore.instance.getConfig().chatUtilitiesConfig;
        if (config.showTime) {
            Component withTime = Component.literal("") // to keep Style.EMPTY for chat message
                    .append(Component.literal(time.format(config.getFormatter())).withStyle(Style.EMPTY.withColor(0xFF808080)))
                    .append(Component.literal(" "))
                    .append(message.content());
            return new GuiMessage(message.addedTime(), withTime, message.signature(), message.source(), message.tag());
        } else {
            return message;
        }
    }

    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V")
    private void onAddMessage(Component contents, @Nullable MessageSignature signature, GuiMessageSource source, @Nullable GuiMessageTag tag, CallbackInfo info) {
        Events.ChatMessageAdded.trigger(contents);
    }
}