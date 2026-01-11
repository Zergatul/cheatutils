package net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline;

import net.caffeinemc.mods.sodium.client.render.model.MutableQuadViewImpl;
import net.caffeinemc.mods.sodium.client.services.PlatformModelEmitter;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class BlockRenderer {

    public void renderModel(BlockStateModel model, BlockState state, BlockPos pos, BlockPos origin) {
        PlatformModelEmitter.getInstance().emitModel(model, null, null, null, null, pos, state, this::bufferDefaultModel);
    }

    public void bufferDefaultModel(BlockModelPart part, Predicate<Direction> cullTest, Consumer<MutableQuadViewImpl> emitter) {}
}