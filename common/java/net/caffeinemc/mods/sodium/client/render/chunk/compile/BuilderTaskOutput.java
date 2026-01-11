package net.caffeinemc.mods.sodium.client.render.chunk.compile;

import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;

public abstract class BuilderTaskOutput {
    public final RenderSection render;

    public BuilderTaskOutput() {
        this.render = new RenderSection();
    }
}