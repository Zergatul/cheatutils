package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderPassBackend;
import com.zergatul.cheatutils.extensions.RenderPassExtension;
import com.zergatul.cheatutils.extensions.RenderPassBackendExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderPass.class)
public abstract class MixinRenderPass implements RenderPassExtension {

    @Final
    @Shadow
    private RenderPassBackend backend;

    @Shadow
    private boolean isClosed;

    @Override
    public void drawInstanced_CU(final int firstVertex, final int vertexCount, final int instanceCount) {
        if (this.isClosed) {
            throw new IllegalStateException("Can't use a closed render pass");
        } else {
            ((RenderPassBackendExtension) this.backend).drawInstanced_CU(firstVertex, vertexCount, instanceCount);
        }
    }
}