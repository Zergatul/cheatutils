package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.configs.ChatUtilitiesConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.utils.TimeWrappedComponent;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(ChatComponent.class)
public abstract class MixinChatComponent {

    @ModifyConstant(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V",
            constant = @Constant(intValue = 100))
    private int onModifyMaxChatHistory(int size) {
        ChatUtilitiesConfig config = ConfigStore.instance.getConfig().chatUtilitiesConfig;
        return config.overrideMessageLimit ? config.messageLimit : size;
    }

    @ModifyArg(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ComponentRenderUtils;wrapComponents(Lnet/minecraft/network/chat/FormattedText;ILnet/minecraft/client/gui/Font;)Ljava/util/List;", ordinal = 0))
    private FormattedText onModifyWrapComponents(FormattedText text) {
        ChatUtilitiesConfig config = ConfigStore.instance.getConfig().chatUtilitiesConfig;
        if (config.showTime) {
            if (text instanceof TimeWrappedComponent component) {
                text = Component.literal("") // to keep Style.EMPTY for chat message
                        .append(Component.literal(component.getTime().format(config.getFormatter())).withStyle(Style.EMPTY.withColor(0xFF808080)))
                        .append(Component.literal(" "))
                        .append(component.unwrap());
            }
        } else {
            if (text instanceof TimeWrappedComponent component) {
                text = component.unwrap();
            }
        }

        return text;
    }

    @ModifyArg(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V", ordinal = 0))
    private Component onModifyMessageComponent(Component component) {
        return new TimeWrappedComponent(component);
    }
}