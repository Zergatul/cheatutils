package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.opengl.GlRenderPass;
import com.zergatul.cheatutils.extensions.GlCommandEncoderExtension;
import com.zergatul.cheatutils.extensions.RenderPassBackendExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GlRenderPass.class)
public abstract class MixinGlRenderPass implements RenderPassBackendExtension {

    @Final
    @Shadow
    private GlCommandEncoder encoder;

    @Override
    public void drawInstanced_CU(int firstVertex, int vertexCount, int instanceCount) {
        ((GlCommandEncoderExtension) this.encoder).executeDrawInstanced_CU((GlRenderPass) (Object) this, firstVertex, vertexCount, instanceCount);
    }
}