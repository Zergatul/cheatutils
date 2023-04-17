package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.accessors.ServerboundMovePlayerPacketAccessor;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerboundMovePlayerPacket.class)
public class MixinServerboundMovePlayerPacket implements ServerboundMovePlayerPacketAccessor {

    @Shadow
    @Final
    @Mutable
    protected boolean onGround;

    @Override
    public void setOnGround_CU(boolean value) {
        this.onGround = value;
    }
}