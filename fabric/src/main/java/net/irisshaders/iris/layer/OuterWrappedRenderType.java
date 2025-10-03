package net.irisshaders.iris.layer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;

public class OuterWrappedRenderType extends RenderType {

    public OuterWrappedRenderType(String name, RenderType wrapped, RenderStateShard extra) {
        super(name, wrapped.bufferSize(),
                wrapped.affectsCrumbling(), shouldSortOnUpload(wrapped), wrapped::setupRenderState, wrapped::clearRenderState);
        throw new AssertionError();
    }

    @Override
    public void draw(MeshData meshData) {
        throw new AssertionError();
    }

    @Override
    public @NotNull VertexFormat format() {
        throw new AssertionError();
    }

    @Override
    public @NotNull VertexFormat.Mode mode() {
        throw new AssertionError();
    }

    @Override
    public @NotNull RenderPipeline pipeline() {
        throw new AssertionError();
    }

    private static boolean shouldSortOnUpload(RenderType type) {
        throw new AssertionError();
    }
}