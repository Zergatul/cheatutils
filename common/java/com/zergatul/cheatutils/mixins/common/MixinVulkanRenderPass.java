package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.extensions.RenderPassBackendExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "com.mojang.blaze3d.vulkan.VulkanRenderPass")
public abstract class MixinVulkanRenderPass implements RenderPassBackendExtension {

    @Override
    public void drawInstanced_CU(int firstVertex, int vertexCount, int instanceCount) {

    }
}