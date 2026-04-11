package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.zergatul.cheatutils.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.resources.Identifier;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;

public class EntityEspOverlayRenderer {

    private final RenderTarget renderTarget;
    private final RenderPipeline pipeline;
    private final GpuBuffer ubo;

    private EntityEspOverlayRenderer() {
        renderTarget = RenderTargets.getEsp();
        pipeline = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "pipeline/entity-esp-overlay"))
                .withBindGroupLayout(BindGroupLayouts.TEXTURE0)
                .withBindGroupLayout(BindGroupLayouts.INPUTS)
                .withVertexShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "screen-quad"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "color-overlay"))
                .withColorTargetState(new ColorTargetState(Optional.of(BlendFunctions.DEFAULT), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_COLOR))
                .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                .build();
        ubo = RenderSystem.getDevice().createBuffer(
                () -> Constants.MOD_ID + ": Entity ESP Overlay UBO",
                GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                16);
    }

    public static EntityEspOverlayRenderer getInstance() {
        return Holder.INSTANCE;
    }

    public void begin() {
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(
                Objects.requireNonNull(renderTarget.getColorTexture()),
                GuiRenderer.CLEAR_COLOR,
                Objects.requireNonNull(renderTarget.getDepthTexture()),
                1.0);
        RenderSystem.outputColorTextureOverride = renderTarget.getColorTextureView();
        RenderSystem.outputDepthTextureOverride = renderTarget.getDepthTextureView();
    }

    public void end(Color color) {
        RenderSystem.outputColorTextureOverride = null;
        RenderSystem.outputDepthTextureOverride = null;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer buffer = stack.malloc(16);
            buffer.putFloat(color.getRed() / 255f);
            buffer.putFloat(color.getGreen() / 255f);
            buffer.putFloat(color.getBlue() / 255f);
            buffer.putFloat(color.getAlpha() / 255f);
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(ubo.slice(), buffer.flip());
        }

        RenderTarget mainRenderTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> Constants.MOD_ID + ": Blit entity overlay",
                Objects.requireNonNull(mainRenderTarget.getColorTextureView()),
                Optional.empty())
        ) {
            renderPass.setPipeline(pipeline);
            renderPass.bindTexture(BindGroupLayouts.TEXTURE0_NAME, renderTarget.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            renderPass.setUniform(BindGroupLayouts.UNIFORM_BLOCK_NAME, ubo);
            renderPass.draw(3, 1, 0, 0);
        }
    }

    private static final class Holder {
        public static final EntityEspOverlayRenderer INSTANCE = new EntityEspOverlayRenderer();
    }
}