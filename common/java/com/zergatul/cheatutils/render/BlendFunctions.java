package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.platform.BlendFactor;

public class BlendFunctions {
    public static final BlendFunction DEFAULT = new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA, BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);
}