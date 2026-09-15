package com.zergatul.cheatutils.render.buffers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.zergatul.cheatutils.render.Position2dColorRenderer;
import com.zergatul.cheatutils.render.Position2dTextureColorRenderer;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RenderBuffers implements AutoCloseable {

    public static final int BACKGROUNDS = -4;
    public static final int ITEMS = 0;
    public static final int ITEM_BAR = 1;
    public static final int FONT_SHADOW = 2;
    public static final int FONT = 4;

    private final Matrix4f matrix;
    private Int2ObjectMap<Position2dColorRenderer.BufferBuilder> color2dMap;
    private Int2ObjectMap<Object2ObjectMap<GpuTextureView, Position2dTextureColorRenderer.BufferBuilder>> texColor2dMap;

    public RenderBuffers(Matrix4f matrix) {
        this.matrix = matrix;
    }

    public Position2dColorRenderer.BufferBuilder getColor2d() {
        return this.getColor2d(RenderBuffers.BACKGROUNDS);
    }

    public Position2dColorRenderer.BufferBuilder getColor2d(int zIndex) {
        createColor2dMapIfRequired();
        return this.color2dMap.computeIfAbsent(zIndex, _ -> new Position2dColorRenderer.BufferBuilder());
    }

    public Position2dTextureColorRenderer.BufferBuilder getTexColor2d(int zIndex, GpuTextureView textureView) {
        createTexColor2MapIfRequired();
        return this.texColor2dMap
                .computeIfAbsent(zIndex, _ -> new Object2ObjectArrayMap<>())
                .computeIfAbsent(textureView, _ -> new Position2dTextureColorRenderer.BufferBuilder());
    }

    public void render(RenderTarget renderTarget) {
        List<LayerRenderTask> tasks = new ArrayList<>();

        if (this.color2dMap != null) {
            this.color2dMap.forEach((zIndex, builder) -> {
                tasks.add(new LayerRenderTask(zIndex, () -> {
                    Position2dColorRenderer.getInstance().draw(renderTarget, matrix, builder);
                    builder.clear();
                }));
            });
        }

        if (this.texColor2dMap != null) {
            this.texColor2dMap.forEach((zIndex, map) -> {
                tasks.add(new LayerRenderTask(zIndex, () -> {
                    for (Map.Entry<GpuTextureView, Position2dTextureColorRenderer.BufferBuilder> entry : map.entrySet()) {
                        Position2dTextureColorRenderer.getInstance().draw(renderTarget, entry.getKey(), matrix, entry.getValue());
                        entry.getValue().clear();
                    }
                }));
            });
        }

        tasks.sort(Comparator.comparingInt(t -> t.zIndex));
        tasks.forEach(t -> t.task.run());
    }

    @Override
    public void close() {
        if (this.color2dMap != null) {
            this.color2dMap.values().forEach(Position2dColorRenderer.BufferBuilder::close);
            this.color2dMap.clear();
        }

        if (this.texColor2dMap != null) {
            this.texColor2dMap.values().forEach(map -> {
                map.values().forEach(Position2dTextureColorRenderer.BufferBuilder::close);
                map.clear();
            });
            this.texColor2dMap.clear();
        }
    }

    private boolean hasTexColor2dData(Object2ObjectMap<GpuTextureView, Position2dTextureColorRenderer.BufferBuilder> map) {
        return map != null && !map.values().stream().allMatch(Position2dTextureColorRenderer.BufferBuilder::isEmpty);
    }

    private void createColor2dMapIfRequired() {
        if (this.color2dMap == null) {
            this.color2dMap = new Int2ObjectArrayMap<>(1);
        }
    }

    private void createTexColor2MapIfRequired() {
        if (this.texColor2dMap == null) {
            this.texColor2dMap = new Int2ObjectArrayMap<>(4);
        }
    }

    private record LayerRenderTask(int zIndex, Runnable task) {}
}