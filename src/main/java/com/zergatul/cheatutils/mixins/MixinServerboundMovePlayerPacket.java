package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.interfaces.ServerboundMovePlayerPacketMixinInterface;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerboundMovePlayerPacket.class)
public class MixinServerboundMovePlayerPacket implements ServerboundMovePlayerPacketMixinInterface {

    @Mutable
    @Shadow
    @Final
    private boolean onGround;

    @Override
    public void setOnGround(boolean value) {
        onGround = value;
    }
}