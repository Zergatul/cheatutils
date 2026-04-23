package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zergatul.cheatutils.render.VertexFormats;
import org.lwjgl.opengl.GL33C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.mojang.blaze3d.opengl.VertexArrayCache$Emulated")
public abstract class MixinVertexArrayCacheEmulated {

    @Inject(method = "bindVertexArray", at = @At("RETURN"))
    private void bindInstancedDivisors(VertexFormat format, GlBuffer vertexBuffer, CallbackInfo ci) {
        if (format == VertexFormats.LINES_INSTANCED || format == VertexFormats.CUBE_LINES_INSTANCED) {
            for (int i = 0; i < format.getElements().size(); i++) {
                GL33C.glVertexAttribDivisor(i, 1);
            }
        }
    }
}