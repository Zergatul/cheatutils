package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.VertexArrayCache;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zergatul.cheatutils.render.VertexFormats;
import org.lwjgl.opengl.GL33C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.mojang.blaze3d.opengl.VertexArrayCache$Emulated")
public abstract class MixinVertexArrayCacheEmulated {

    @Inject(method = "bindVertexArray", at = @At("RETURN"))
    private void onAfterBindVertexArraySetupInstancedDivisors(
            VertexFormat[] vertexBindings,
            GpuBufferSlice[] vertexBuffers,
            VertexArrayCache.VertexArray lastBoundVertexArray,
            CallbackInfoReturnable<VertexArrayCache.VertexArray> info
    ) {
        if (VertexFormats.isInstanced(vertexBindings[0])) {
            for (int i = 0; i < vertexBindings[0].getElements().size(); i++) {
                GL33C.glVertexAttribDivisor(i, 1);
            }
        }
    }
}