package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.VertexArrayCache;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zergatul.cheatutils.render.VertexFormats;
import org.lwjgl.opengl.ARBVertexAttribBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.mojang.blaze3d.opengl.VertexArrayCache$Separate")
public abstract class MixinVertexArrayCacheSeparate {

    @Inject(method = "bindVertexArray", at = @At("RETURN"))
    private void onBindVertexArraySetupInstancedDivisors(
            VertexFormat[] vertexBindings,
            GpuBufferSlice[] vertexBuffers,
            VertexArrayCache.VertexArray lastBoundVertexArray,
            CallbackInfoReturnable<VertexArrayCache.VertexArray> info
    ) {
        if (VertexFormats.isInstanced(vertexBindings[0])) {
            ARBVertexAttribBinding.glVertexBindingDivisor(0, 1);
        }
    }
}