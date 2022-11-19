package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.interfaces.ServerboundMovePlayerPacketMixinInterface;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerMoveC2SPacket.class)
public abstract class MixinPlayerMoveC2SPacket implements ServerboundMovePlayerPacketMixinInterface {

    @Mutable
    @Shadow
    @Final
    private boolean onGround;

    @Override
    public void setOnGround(boolean value) {
        onGround = value;
    }
}
