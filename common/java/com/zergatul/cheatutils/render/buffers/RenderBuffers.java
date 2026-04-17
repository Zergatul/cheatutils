package com.zergatul.cheatutils.render.buffers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.zergatul.cheatutils.render.Position2dColorRenderer;
import com.zergatul.cheatutils.render.Position2dTextureColorRenderer;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.joml.Matrix4f;

public class RenderBuffers {

    private final Matrix4f matrix;
    // add z-index
    private Position2dColorRenderer.BufferBuilder color2d;
    private Object2ObjectMap<GpuTextureView, Position2dTextureColorRenderer.BufferBuilder> texColor2dBack;
    private Object2ObjectMap<GpuTextureView, Position2dTextureColorRenderer.BufferBuilder> texColor2dFront;

    public RenderBuffers(Matrix4f matrix) {
        this.matrix = matrix;
    }

    public Position2dColorRenderer.BufferBuilder getColor2d() {
        if (color2d == null) {
            color2d = new Position2dColorRenderer.BufferBuilder();
        }
        return color2d;
    }

    public Position2dTextureColorRenderer.BufferBuilder getTexColor2dBack(GpuTextureView textureView) {
        if (texColor2dBack == null) {
            // use simple array map, since we shouldn't have a lot of entries here
            texColor2dBack = new Object2ObjectArrayMap<>();
        }
        if (!texColor2dBack.containsKey(textureView)) {
            texColor2dBack.put(textureView, new Position2dTextureColorRenderer.BufferBuilder());
        }
        return texColor2dBack.get(textureView);
    }

    public Position2dTextureColorRenderer.BufferBuilder getTexColor2dFront(GpuTextureView textureView) {
        if (texColor2dFront == null) {
            // use simple array map, since we shouldn't have a lot of entries here
            texColor2dFront = new Object2ObjectArrayMap<>();
        }
        if (!texColor2dFront.containsKey(textureView)) {
            texColor2dFront.put(textureView, new Position2dTextureColorRenderer.BufferBuilder());
        }
        return texColor2dFront.get(textureView);
    }

    public void render(RenderTarget renderTarget) {
        if (color2d != null && !color2d.isEmpty()) {
            Position2dColorRenderer.getInstance().draw(renderTarget, matrix, color2d);
            color2d.clear();
        }

        if (hasTexColor2dData(texColor2dBack)) {
            for (GpuTextureView textureView : texColor2dBack.keySet()) {
                Position2dTextureColorRenderer.BufferBuilder buffer = texColor2dBack.get(textureView);
                Position2dTextureColorRenderer.getInstance().draw(renderTarget, textureView, matrix, buffer);
                buffer.clear();
            }
        }

        if (hasTexColor2dData(texColor2dFront)) {
            for (GpuTextureView textureView : texColor2dFront.keySet()) {
                Position2dTextureColorRenderer.BufferBuilder buffer = texColor2dFront.get(textureView);
                Position2dTextureColorRenderer.getInstance().draw(renderTarget, textureView, matrix, buffer);
                buffer.clear();
            }
        }
    }

    private boolean hasTexColor2dData(Object2ObjectMap<GpuTextureView, Position2dTextureColorRenderer.BufferBuilder> map) {
        return map != null && !map.values().stream().allMatch(Position2dTextureColorRenderer.BufferBuilder::isEmpty);
    }
}