package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.interfaces.ClientboundPlayerInfoPacketMixinInterface;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ClientboundPlayerInfoPacket.class)
public class MixinClientboundPlayerInfoPacket implements ClientboundPlayerInfoPacketMixinInterface {

    @Mutable
    @Shadow
    @Final
    private List<ClientboundPlayerInfoPacket.PlayerUpdate> entries;

    @Override
    public void setEntries(List<ClientboundPlayerInfoPacket.PlayerUpdate> entries) {
        this.entries = entries;
    }
}
