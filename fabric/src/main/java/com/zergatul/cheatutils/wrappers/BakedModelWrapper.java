package com.zergatul.cheatutils.wrappers;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

import java.util.List;

public class BakedModelWrapper {

    public static List<BakedQuad> getQuads(BakedModel model, Direction direction, RandomSource random) {
        return model.getQuads(null, direction, random);
    }
}