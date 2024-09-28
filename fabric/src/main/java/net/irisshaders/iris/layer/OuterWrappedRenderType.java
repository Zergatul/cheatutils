package net.irisshaders.iris.layer;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

public class OuterWrappedRenderType extends RenderType {

    public OuterWrappedRenderType(String string, VertexFormat vertexFormat, VertexFormat.Mode mode, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
        super(string, vertexFormat, mode, i, bl, bl2, runnable, runnable2);
        throw new AssertionError();
    }

    public RenderType unwrap() {
        throw new AssertionError();
    }
}