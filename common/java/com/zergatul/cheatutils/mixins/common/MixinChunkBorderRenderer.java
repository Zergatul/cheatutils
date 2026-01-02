package com.zergatul.cheatutils.mixins.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.zergatul.cheatutils.modules.esp.FreeCam;
import net.minecraft.client.renderer.debug.ChunkBorderRenderer;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkBorderRenderer.class)
public abstract class MixinChunkBorderRenderer {

    @ModifyExpressionValue(
            method = "emitGizmos",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/SectionPos;of(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/SectionPos;"))
    private SectionPos func(SectionPos original) {
        if (FreeCam.instance.isActive()) {
            return SectionPos.of(new Vec3(FreeCam.instance.getX(), FreeCam.instance.getY(), FreeCam.instance.getZ()));
        } else {
            return original;
        }
    }
}