package com.zergatul.cheatutils.mixins.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlCommandEncoder")
public abstract class MixinGlCommandEncoder {

    @ModifyArgs(
            method = "copyTextureToTexture",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/DirectStateAccess;blitFrameBuffers(IIIIIIIIIIII)V"))
    private void fixBrokenCopyTextureToTexture(Args args) {
        args.set(4, args.<Integer>get(2) + args.<Integer>get(4));
        args.set(5, args.<Integer>get(3) + args.<Integer>get(5));
        args.set(8, args.<Integer>get(6) + args.<Integer>get(8));
        args.set(9, args.<Integer>get(7) + args.<Integer>get(9));
    }
}