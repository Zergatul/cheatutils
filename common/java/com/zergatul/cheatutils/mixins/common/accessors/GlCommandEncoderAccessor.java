package com.zergatul.cheatutils.mixins.common.accessors;

import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.opengl.GlRenderPass;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// TODO: not needed???
@Mixin(GlCommandEncoder.class)
public interface GlCommandEncoderAccessor {

    @Invoker("executeDraw")
    void executeDraw_CU(
            GlRenderPass renderPass,
            int baseVertex,
            int firstIndex,
            int drawCount,
            VertexFormat.IndexType indexType,
            int instanceCount);
}