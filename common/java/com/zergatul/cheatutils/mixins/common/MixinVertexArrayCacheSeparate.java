package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zergatul.cheatutils.render.VertexFormats;
import org.lwjgl.opengl.ARBVertexAttribBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.mojang.blaze3d.opengl.VertexArrayCache$Separate")
public abstract class MixinVertexArrayCacheSeparate {

    @Inject(method = "bindVertexArray", at = @At("RETURN"))
    private void bindInstancedDivisors(VertexFormat format, GlBuffer vertexBuffer, CallbackInfo ci) {
        if (format == VertexFormats.LINES_INSTANCED || format == VertexFormats.CUBE_LINES_INSTANCED) {
            ARBVertexAttribBinding.glVertexBindingDivisor(0, 1);
        }
    }
}