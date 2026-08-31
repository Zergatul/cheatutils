package me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline;

import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.model.color.ColorProvider;
import me.jellysquid.mods.sodium.client.model.quad.BakedQuadView;
import net.minecraft.world.level.block.state.BlockState;

public class BlockRenderer {
    public void renderModel(BlockRenderContext context, ChunkBuildBuffers buffers) {
        int[] colors = this.getVertexColors(context, null, null);
        this.writeGeometry(colors);
    }

    private int[] getVertexColors(
            BlockRenderContext context,
            ColorProvider<BlockState> colorProvider,
            BakedQuadView quad
    ) {
        return null;
    }

    private void writeGeometry(int[] colors) {}
}