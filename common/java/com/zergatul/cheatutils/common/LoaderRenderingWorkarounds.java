package com.zergatul.cheatutils.common;

import com.zergatul.cheatutils.webui.BlockModelApi;
import net.minecraft.client.renderer.feature.submit.SubmitNode;

import java.util.List;

public interface LoaderRenderingWorkarounds {
    default void extractQuads(SubmitNode submission, List<BlockModelApi.Quad> output) {}
}