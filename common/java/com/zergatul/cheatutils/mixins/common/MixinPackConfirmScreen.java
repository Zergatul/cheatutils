package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.PrivacyConfig;
import com.zergatul.mixin.LocalVariable;
import com.zergatul.mixin.ModifyMethodReturnValue;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ClientCommonPacketListenerImpl.PackConfirmScreen.class)
public abstract class MixinPackConfirmScreen {

    @ModifyMethodReturnValue(
            method = "<init>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientCommonPacketListenerImpl;preparePackPrompt(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;)Lnet/minecraft/network/chat/Component;"))
    private static Component onModifyMessage(Component original, @LocalVariable(ordinal = 0) List<ClientCommonPacketListenerImpl.PackConfirmScreen.PendingRequest> requests) {
        PrivacyConfig config = ConfigStore.instance.getConfig().privacyConfig;
        if (!config.displayResourcePackUrls) {
            return original;
        }

        MutableComponent result = Component.empty().append(original).append("\n");
        for (ClientCommonPacketListenerImpl.PackConfirmScreen.PendingRequest request : requests) {
            result = result
                    .append("\n")
                    .append(Component.literal(request.url().toString()).withStyle(ChatFormatting.AQUA));
        }

        return result;
    }
}