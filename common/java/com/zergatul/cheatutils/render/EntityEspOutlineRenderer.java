package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.zergatul.cheatutils.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

public class EntityEspOutlineRenderer {

    private final RenderTarget renderTarget;
    private final RenderPipeline pipeline;
    private final GpuBuffer ubo;

    private EntityEspOutlineRenderer() {
        renderTarget = RenderTargets.getEsp();
        pipeline = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pipeline/entity-esp-outline"))
                .withBindGroupLayout(BindGroupLayouts.TEXTURE0)
                .withBindGroupLayout(BindGroupLayouts.INPUTS)
                .withVertexShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "screen-quad"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "entity-outline"))
                .withColorTargetState(new ColorTargetState(Optional.of(BlendFunctions.DEFAULT), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_COLOR))
                .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                .build();
        ubo = RenderSystem.getDevice().createBuffer(() -> "Entity ESP Outline UBO", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, 24);
    }

    public static EntityEspOutlineRenderer getInstance() {
        return Holder.INSTANCE;
    }

    public void begin() {
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
                Objects.requireNonNull(renderTarget.getColorTexture()),
                GuiRenderer.CLEAR_COLOR,
                Objects.requireNonNull(renderTarget.getDepthTexture()),
                0.0);
    }

    public void draw(FeatureRenderDispatcher.PreparedFrame frame) {
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> Constants.MOD_ID + ": Entity outline mask",
                Objects.requireNonNull(renderTarget.getColorTextureView()),
                Optional.empty(),
                Objects.requireNonNull(renderTarget.getDepthTextureView()),
                OptionalDouble.empty())
        ) {
            RenderSystem.bindDefaultUniforms(renderPass);
            frame.executeOutline(renderPass);
        }
    }

    public void end(Color color) {
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
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Blit entity outline target",
                Objects.requireNonNull(mainRenderTarget.getColorTextureView()),
                Optional.empty())
        ) {
            renderPass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
            renderPass.setUniform(BindGroupLayouts.TEXTURE0_NAME, renderTarget.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            renderPass.setUniform(BindGroupLayouts.UNIFORM_BLOCK_NAME, ubo);
            renderPass.draw(3, 1, 0, 0);
        }
    }

    private static final class Holder {
        public static final EntityEspOutlineRenderer INSTANCE = new EntityEspOutlineRenderer();
    }
}