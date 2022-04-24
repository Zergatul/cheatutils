package com.zergatul.cheatutils.interfaces;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;

import java.util.List;

public interface ClientboundPlayerInfoPacketMixinInterface {
    void setEntries(List<ClientboundPlayerInfoPacket.PlayerUpdate> entries);
}
