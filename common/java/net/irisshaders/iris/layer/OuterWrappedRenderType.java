package net.irisshaders.iris.layer;

import net.minecraft.client.renderer.RenderType;

public class OuterWrappedRenderType {
    public RenderType unwrap() {
        throw new AssertionError();
    }
}