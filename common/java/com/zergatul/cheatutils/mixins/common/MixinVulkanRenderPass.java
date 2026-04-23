package com.zergatul.cheatutils.mixins.common;

import com.mojang.blaze3d.vulkan.VulkanRenderPass;
import com.mojang.blaze3d.vulkan.VulkanRenderPipeline;
import com.zergatul.cheatutils.extensions.RenderPassBackendExtension;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VulkanRenderPass.class)
public abstract class MixinVulkanRenderPass implements RenderPassBackendExtension {

    @Shadow
    protected VulkanRenderPipeline pipeline;

    @Shadow
    protected abstract void pushDescriptors();

    @Shadow
    protected abstract VkCommandBuffer secondaryCommandBuffer();

    @Override
    public void drawInstanced_CU(int firstVertex, int vertexCount, int instanceCount) {
        if (this.pipeline != null && this.pipeline.isValid()) {
            this.pushDescriptors();
            VK12.vkCmdDraw(this.secondaryCommandBuffer(), vertexCount, instanceCount, firstVertex, 0);
        } else {
            throw new IllegalStateException("Pipeline is missing or not valid");
        }
    }
}