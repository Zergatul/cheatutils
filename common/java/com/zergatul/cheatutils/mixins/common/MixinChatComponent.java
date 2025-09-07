package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ChatUtilitiesConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalDateTime;
import java.util.*;

@Mixin(ChatComponent.class)
public abstract class MixinChatComponent {

    @Unique
    private final Map<GuiMessage, LocalDateTime> messageTimeMap = new WeakHashMap<>();

    @Unique
    private GuiMessage currentProcessingMessage;

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
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At(value = "STORE"))
    private GuiMessage onGuiMessageCreated(GuiMessage message) {
        messageTimeMap.put(message, LocalDateTime.now());
        return message;
    }

    @Inject(method = "addMessageToDisplayQueue", at = @At("HEAD"))
    private void onAddMessageToDisplayQueue(GuiMessage message, CallbackInfo info) {
        currentProcessingMessage = message;
    }

    @ModifyArg(
            method = "addMessageToDisplayQueue",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ComponentRenderUtils;wrapComponents(Lnet/minecraft/network/chat/FormattedText;ILnet/minecraft/client/gui/Font;)Ljava/util/List;"),
            index = 0)
    private FormattedText onModifyWrapComponents(FormattedText text) {
        if (currentProcessingMessage == null) {
            return text;
        }

        LocalDateTime time = messageTimeMap.get(currentProcessingMessage);
        if (time == null) {
            return text;
        }

        ChatUtilitiesConfig config = ConfigStore.instance.getConfig().chatUtilitiesConfig;
        if (config.showTime) {
            return Component.literal("") // to keep Style.EMPTY for chat message
                    .append(Component.literal(time.format(config.getFormatter())).withStyle(Style.EMPTY.withColor(0xFF808080)))
                    .append(Component.literal(" "))
                    .append(currentProcessingMessage.content());
        } else {
            return text;
        }
    }

    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V")
    private void onAddMessage(Component component, MessageSignature signature, GuiMessageTag tag, CallbackInfo info) {
        Events.ChatMessageAdded.trigger(component);
    }
}