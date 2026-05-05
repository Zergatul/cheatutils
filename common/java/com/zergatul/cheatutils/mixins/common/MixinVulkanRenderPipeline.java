package com.zergatul.cheatutils.mixins.common;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vulkan.VulkanRenderPipeline;
import com.zergatul.cheatutils.render.VertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(VulkanRenderPipeline.class)
public abstract class MixinVulkanRenderPipeline {

    @ModifyArg(
            method = "compile",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VkVertexInputBindingDescription;inputRate(I)Lorg/lwjgl/vulkan/VkVertexInputBindingDescription;"))
    private static int onModifyInputRate(
            int original,
            @Local(name = "pipeline") RenderPipeline pipeline
    ) {
        if (VertexFormats.isInstanced(pipeline.getVertexFormatBinding(0))) {
            return 1;
        } else {
            return original;
        }
    }
}