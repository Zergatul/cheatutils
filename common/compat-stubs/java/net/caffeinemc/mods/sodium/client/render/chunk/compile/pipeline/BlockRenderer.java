package net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline;

import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BlockRenderer {
    public void renderModel(BakedModel model, BlockState state, BlockPos pos, BlockPos origin) {}

    protected void processQuad(MutableQuadViewImpl quad) {
        this.colorizeQuad(quad, 0);
    }

    private void colorizeQuad(MutableQuadViewImpl quad, int colorIndex) {}
}