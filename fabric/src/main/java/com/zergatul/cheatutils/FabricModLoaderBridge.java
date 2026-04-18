package com.zergatul.cheatutils;

import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.cheatutils.webui.BlockModelApi;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricSubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollection;

import java.util.List;

public class FabricModLoaderBridge implements ModLoaderBridge {

    @Override
    public void extractAdditionalQuads(SubmitNodeCollection collection, List<BlockModelApi.Quad> output) {
        for (FabricSubmitNodeCollection.ExtendedBlockModelSubmit submission : collection.getExtendedBlockModelSubmits()) {
            if (submission.mesh() != null) {
                submission.mesh().forEach(view -> {
                    BlockModelApi.Vertex[] vertices = new BlockModelApi.Vertex[4];
                    for (int i = 0; i < 4; i++) {
                        vertices[i] = new BlockModelApi.Vertex();

                        vertices[i].x = view.x(i) - 0.5f;
                        vertices[i].y = view.y(i) - 0.5f;
                        vertices[i].z = view.z(i) - 0.5f;

                        int color = view.color(i);
                        boolean isTinted = view.tintIndex() != -1 && view.tintIndex() < submission.tintLayers().length;
                        if (isTinted) {
                            color = ColorUtils.Int.multiply(color, submission.tintLayers()[view.tintIndex()]);
                        }

                        vertices[i].r = ColorUtils.Int.r(color);
                        vertices[i].g = ColorUtils.Int.g(color);
                        vertices[i].b = ColorUtils.Int.b(color);
                        vertices[i].a = ColorUtils.Int.a(color);

                        vertices[i].u = view.u(i);
                        vertices[i].v = view.v(i);
                    }

                    output.add(
                            new BlockModelApi.Quad(
                                    view.atlas().getTextureLocation().toString(),
                                    vertices[0],
                                    vertices[1],
                                    vertices[2],
                                    vertices[3]));
                });
            }
        }
    }
}