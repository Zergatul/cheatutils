package net.irisshaders.iris.layer;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

/**
 * Compile-time stub for optional Iris integration. This class is excluded from produced jars.
 */
public class OuterWrappedRenderType extends RenderType {

    public OuterWrappedRenderType(
            String name,
            VertexFormat format,
            VertexFormat.Mode mode,
            int bufferSize,
            boolean affectsCrumbling,
            boolean sortOnUpload,
            Runnable setupState,
            Runnable clearState
    ) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
        throw new AssertionError();
    }

    public RenderType unwrap() {
        throw new AssertionError();
    }
}