package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.shaders.UniformType;

public class BindGroupLayouts {

    public static final String TEXTURE0_NAME = "Texture0";
    public static final String UNIFORM_BLOCK_NAME = "Inputs";

    public static final BindGroupLayout TEXTURE0 = BindGroupLayout.builder()
            .withSampler(TEXTURE0_NAME)
            .build();

    public static final BindGroupLayout INPUTS = BindGroupLayout.builder()
            .withUniform(UNIFORM_BLOCK_NAME, UniformType.UNIFORM_BUFFER)
            .build();
}