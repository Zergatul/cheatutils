package com.zergatul.cheatutils.extensions;

import com.mojang.blaze3d.opengl.GlRenderPass;

public interface GlCommandEncoderExtension {
    void executeDrawInstanced_CU(GlRenderPass renderPass, int firstVertex, int vertexCount, int instanceCount);
}