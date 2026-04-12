package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Matrix4fc;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class CustomGlyph implements TextRenderable {
    @Override
    public void render(Matrix4fc pose, VertexConsumer buffer, int packedLightCoords, boolean flat) {
        throw new AssertionError();
    }

    @Override
    public RenderType renderType(Font.DisplayMode displayMode) {
        throw new AssertionError();
    }

    @Override
    public GpuTextureView textureView() {
        throw new AssertionError();
    }

    @Override
    public RenderPipeline guiPipeline() {
        throw new AssertionError();
    }

    @Override
    public float left() {
        throw new AssertionError();
    }

    @Override
    public float top() {
        throw new AssertionError();
    }

    @Override
    public float right() {
        throw new AssertionError();
    }

    @Override
    public float bottom() {
        throw new AssertionError();
    }
}