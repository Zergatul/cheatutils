package net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks;

import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.BuilderTaskOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;

public abstract class ChunkBuilderTask<OUTPUT extends BuilderTaskOutput> {

    protected final RenderSection render;

    protected ChunkBuilderTask() {
        this.render = new RenderSection();
    }

    public abstract OUTPUT execute(ChunkBuildContext context, CancellationToken cancellationToken);
}