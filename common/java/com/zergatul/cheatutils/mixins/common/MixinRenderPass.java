package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.systems.RenderPass;
import com.zergatul.cheatutils.extensions.RenderPassExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RenderPass.class)
public abstract class MixinRenderPass implements RenderPassExtension {

    @Override
    public void drawInstanced_CU(final int firstVertex, final int vertexCount, final int instanceCount) {

    }
}