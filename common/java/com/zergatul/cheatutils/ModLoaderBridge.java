package com.zergatul.cheatutils;

import com.zergatul.cheatutils.webui.BlockModelApi;
import net.minecraft.client.renderer.SubmitNodeCollection;

import java.util.List;

public interface ModLoaderBridge {
    void extractAdditionalQuads(SubmitNodeCollection collection, List<BlockModelApi.Quad> output);
}