package com.zergatul.cheatutils.mixins.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.PrivacyConfig;
import com.zergatul.cheatutils.helpers.MixinClientPacketListenerHelper;
import com.zergatul.cheatutils.modules.scripting.Debugging;
import com.zergatul.cheatutils.modules.utilities.Privacy;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

@Mixin(ClientCommonPacketListenerImpl.class)
public class MixinClientCommonPacketListenerImpl {

    @Shadow
    @Final
    protected Connection connection;

    @ModifyArg(
            method = "handleDisconnect(Lnet/minecraft/network/protocol/common/ClientboundDisconnectPacket;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;disconnect(Lnet/minecraft/network/chat/Component;)V"),
            index = 0)
    private Component onDisconnect(Component component) {
        if (MixinClientPacketListenerHelper.appendDisconnectMessage == null) {
            return component;
        }

        MutableComponent replacement = MutableComponent.create(component.getContents());
        String message = MixinClientPacketListenerHelper.appendDisconnectMessage;
        MixinClientPacketListenerHelper.appendDisconnectMessage = null;
        return replacement.append("\n").append(message);
    }

    @Inject(
            method = "handleResourcePackPush",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V", shift = At.Shift.AFTER))
    private void onBeforePushResourcePack(ClientboundResourcePackPushPacket packet, CallbackInfo info) {
        PrivacyConfig config = ConfigStore.instance.getConfig().privacyConfig;
        if (!config.logResourcePackDetails) {
            return;
        }

        Debugging.instance.addMessage(String.format(
                "Requested resource pack:\n\tURL=%s\n\tUUID=%s\n\tHash=%s\n\tRequired=%s\n\tPrompt=%s",
                packet.url(),
                packet.id(),
                packet.hash(),
                packet.required(),
                packet.prompt().orElse(Component.empty()).getString()));
    }

    @Inject(
            method = "handleResourcePackPush",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;)V"),
            cancellable = true)
    private void onPushResourcePackInvalidUrl(ClientboundResourcePackPushPacket packet, CallbackInfo info) {
        PrivacyConfig config = ConfigStore.instance.getConfig().privacyConfig;
        if (!config.disconnectOnMaliciousResourcePackBehavior) {
            return;
        }

        Component message = Component.literal("")
                .append(Component.literal("Server sent invalid resource pack URL: ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(packet.url()).withStyle(ChatFormatting.GREEN));
        this.connection.disconnect(Privacy.instance.createDisconnectMessage(message));
        info.cancel();
    }

    @Inject(
            method = "handleResourcePackPush",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/common/ClientboundResourcePackPushPacket;hash()Ljava/lang/String;"),
            cancellable = true)
    private void onPushResourcePackValidUrl(
            ClientboundResourcePackPushPacket packet,
            CallbackInfo info,
            @Local(name = "url") URL url
    ) {
        PrivacyConfig config = ConfigStore.instance.getConfig().privacyConfig;
        if (!config.disconnectOnMaliciousResourcePackBehavior) {
            return;
        }

        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(url.getHost());
        } catch (UnknownHostException e) {
            Component message = Component.literal("")
                    .append(Component.literal("Server sent resource pack URL with unresolvable host: ").withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(url.getHost()).withStyle(ChatFormatting.GREEN))
                    .append("\n")
                    .append(Component.literal(url.toString()).withStyle(ChatFormatting.BLUE));
            this.connection.disconnect(Privacy.instance.createDisconnectMessage(message));
            info.cancel();
            return;
        }

        for (InetAddress address : addresses) {
            if (address.isLoopbackAddress()) {
                Component message = Component.literal("")
                        .append(Component.literal("Server sent resource pack URL with loopback host: ").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(address.toString()).withStyle(ChatFormatting.GREEN))
                        .append("\n")
                        .append(Component.literal(url.toString()).withStyle(ChatFormatting.BLUE));
                this.connection.disconnect(Privacy.instance.createDisconnectMessage(message));
                info.cancel();
                return;
            }
            if (address.isSiteLocalAddress() || address.isLinkLocalAddress()) {
                Component message = Component.literal("")
                        .append(Component.literal("Server sent resource pack URL with LAN host: ").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(address.toString()).withStyle(ChatFormatting.GREEN))
                        .append("\n")
                        .append(Component.literal(url.toString()).withStyle(ChatFormatting.BLUE));
                this.connection.disconnect(Privacy.instance.createDisconnectMessage(message));
                info.cancel();
                return;
            }
        }
    }

    @ModifyExpressionValue(
            method = "handleResourcePackPush",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ServerData;getResourcePackStatus()Lnet/minecraft/client/multiplayer/ServerData$ServerPackStatus;"))
    private ServerData.ServerPackStatus onModifyServerPackStatus(ServerData.ServerPackStatus original) {
        if (original != ServerData.ServerPackStatus.ENABLED) {
            return original;
        }

        PrivacyConfig config = ConfigStore.instance.getConfig().privacyConfig;
        return config.alwaysPromptOnResourcePack ? ServerData.ServerPackStatus.PROMPT : original;
    }
}