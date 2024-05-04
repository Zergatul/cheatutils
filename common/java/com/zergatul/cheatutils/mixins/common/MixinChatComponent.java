package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ChatUtilitiesConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.utils.TimeWrappedComponent;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class MixinChatComponent {

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
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private Component onModifyMessageComponent(Component component) {
        return new TimeWrappedComponent(component);
    }

    @ModifyArg(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/GuiMessage;<init>(ILnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V"))
    private Component onModifyWrapComponents(Component component) {
        ChatUtilitiesConfig config = ConfigStore.instance.getConfig().chatUtilitiesConfig;
        if (config.showTime) {
            if (component instanceof TimeWrappedComponent wrapped) {
                component = Component.literal("") // to keep Style.EMPTY for chat message
                        .append(Component.literal(wrapped.getTime().format(config.getFormatter())).withStyle(Style.EMPTY.withColor(0xFF808080)))
                        .append(Component.literal(" "))
                        .append(wrapped.unwrap());
            }
        } else {
            if (component instanceof TimeWrappedComponent wrapped) {
                component = wrapped.unwrap();
            }
        }

        return component;
    }

    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V")
    private void onAddMessage(Component component, MessageSignature signature, GuiMessageTag tag, CallbackInfo info) {
        if (component instanceof TimeWrappedComponent wrapped) {
            Events.ChatMessageAdded.trigger(wrapped.unwrap());
        } else {
            Events.ChatMessageAdded.trigger(component);
        }
    }
}