package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zergatul.cheatutils.ModMain;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalInt;

public class EntityEspOutlineRenderer {

    private final RenderTarget renderTarget;
    private final RenderPipeline pipeline;
    private final GpuBuffer ubo;

    private EntityEspOutlineRenderer() {
        renderTarget = RenderTargets.getEsp();
        pipeline = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(ModMain.MODID, "pipeline/entity-esp-outline"))
                .withBindGroupLayout(BindGroupLayouts.TEXTURE0)
                .withBindGroupLayout(BindGroupLayouts.INPUTS)
                .withVertexShader(Identifier.fromNamespaceAndPath(ModMain.MODID, "screen-quad"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(ModMain.MODID, "entity-outline"))
                .withColorTargetState(new ColorTargetState(Optional.of(BlendFunctions.DEFAULT), ColorTargetState.WRITE_COLOR))
                .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
                .build();
        ubo = RenderSystem.getDevice().createBuffer(() -> "Entity ESP Outline UBO", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, 24);
    }

    public static EntityEspOutlineRenderer getInstance() {
        return Holder.INSTANCE;
    }

    public void begin() {
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(renderTarget.getColorTexture(), 0, renderTarget.getDepthTexture(), 1.0);
        RenderSystem.outputColorTextureOverride = renderTarget.getColorTextureView();
        RenderSystem.outputDepthTextureOverride = renderTarget.getDepthTextureView();
    }

    public void end(Color color) {
        RenderSystem.outputColorTextureOverride = null;
        RenderSystem.outputDepthTextureOverride = null;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = stack.malloc(24);
            buffer.putFloat(color.getRed() / 255f);
            buffer.putFloat(color.getGreen() / 255f);
            buffer.putFloat(color.getBlue() / 255f);
            buffer.putFloat(color.getAlpha() / 255f);
            buffer.putFloat(1f / renderTarget.width);
            buffer.putFloat(1f / renderTarget.height);
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(ubo.slice(), buffer.flip());
        }

        RenderTarget mainRenderTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Blit entity outline target", mainRenderTarget.getColorTextureView(), OptionalInt.empty())) {
            renderPass.setPipeline(pipeline);
            renderPass.bindTexture(BindGroupLayouts.TEXTURE0_NAME, renderTarget.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            renderPass.setUniform(BindGroupLayouts.UNIFORM_BLOCK_NAME, ubo);
            renderPass.draw(0, 3);
        }
    }

    private static final class Holder {
        public static final EntityEspOutlineRenderer INSTANCE = new EntityEspOutlineRenderer();
    }
}