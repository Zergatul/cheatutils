package com.zergatul.cheatutils.common;

import com.zergatul.cheatutils.modules.esp.block.web.BlockModelWebApi;
import net.minecraft.client.renderer.feature.submit.SubmitNode;

import java.util.List;

public interface LoaderRenderingWorkarounds {
    default void extractQuads(SubmitNode submission, List<BlockModelWebApi.Quad> output) {}
}