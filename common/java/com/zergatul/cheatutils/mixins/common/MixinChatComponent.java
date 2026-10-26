package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.modules.ChatUtilitiesConfig;
import com.zergatul.cheatutils.modules.utilities.ChatUtilities;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class MixinChatComponent {

    @ModifyConstant(
            method = "addMessageToQueue",
            constant = @Constant(intValue = 100))
    private int addMessageToQueueModifyMaxChatHistory(int size) {
        ChatUtilitiesConfig config = ChatUtilities.INSTANCE.getSettings().get();
        return config.overrideMessageLimit() ? config.messageLimit() : size;
    }

    @ModifyConstant(
            method = "addMessageToDisplayQueue",
            constant = @Constant(intValue = 100))
    private int addMessageToDisplayQueueModifyMaxChatHistory(int size) {
        ChatUtilitiesConfig config = ChatUtilities.INSTANCE.getSettings().get();
        return config.overrideMessageLimit() ? config.messageLimit() : size;
    }

    @ModifyVariable(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
            at = @At(value = "STORE"),
            name = "message")
    private GuiMessage onGuiMessageCreated(GuiMessage message) {
        ChatUtilities.INSTANCE.onGuiMessageCreated(message);
        return message;
    }

    @ModifyVariable(
            at = @At("HEAD"),
            method = "addMessageToDisplayQueue",
            argsOnly = true)
    private GuiMessage onModifyMessageBeforeWrapping(GuiMessage message) {
        return ChatUtilities.INSTANCE.onGuiMessageUpdate(message);
    }

    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V")
    private void onAddMessage(Component contents, @Nullable MessageSignature signature, GuiMessageSource source, @Nullable GuiMessageTag tag, CallbackInfo info) {
        Events.ChatMessageAdded.trigger(contents);
    }
}