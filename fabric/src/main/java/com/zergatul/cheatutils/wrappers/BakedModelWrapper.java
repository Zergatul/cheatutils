package com.zergatul.cheatutils.wrappers;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BakedModelWrapper {

    public static List<BakedQuad> getQuads(BakedModel model, BlockState state, Direction direction, RandomSource random) {
        return model.getQuads(state, direction, random);
    }
}