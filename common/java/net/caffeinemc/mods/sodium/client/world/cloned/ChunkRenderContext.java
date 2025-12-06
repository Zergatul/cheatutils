package net.caffeinemc.mods.sodium.client.world.cloned;

import net.minecraft.core.SectionPos;

public class ChunkRenderContext {

    private final SectionPos origin;

    public ChunkRenderContext() {
        throw new AssertionError();
    }

    public SectionPos getOrigin() {
        return this.origin;
    }
}