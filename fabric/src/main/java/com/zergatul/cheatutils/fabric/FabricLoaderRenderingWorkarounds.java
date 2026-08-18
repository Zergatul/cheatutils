package com.zergatul.cheatutils.fabric;

import com.zergatul.cheatutils.common.LoaderRenderingWorkarounds;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.cheatutils.webui.BlockModelApi;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.client.renderer.v1.render.submit.ExtendedBlockModelSubmit;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import org.joml.Vector3f;

import java.util.List;

public class FabricLoaderRenderingWorkarounds implements LoaderRenderingWorkarounds {

    public static final LoaderRenderingWorkarounds INSTANCE = new FabricLoaderRenderingWorkarounds();

    private FabricLoaderRenderingWorkarounds() {}

    @Override
    public void extractQuads(SubmitNode submission, List<BlockModelApi.Quad> output) {
        if (submission instanceof ExtendedBlockModelSubmit blockModel) {
            BlockModelApi.extractBlockModelQuads(
                    blockModel.modelParts(),
                    blockModel.tintLayers(),
                    blockModel.tintColor(),
                    output);
            if (blockModel.mesh() != null) {
                blockModel.mesh().forEach(quad -> extractQuad(blockModel, quad, output));
            }
        }
    }

    private void extractQuad(ExtendedBlockModelSubmit submission, QuadView quad, List<BlockModelApi.Quad> output) {
        if (submission.renderTypeFunction().apply(quad.chunkLayer()) == null) {
            return;
        }

        String textureLocation = quad.atlas().getTextureLocation().toString();
        int color = BlockModelApi.getBlockModelQuadColor(quad.tintIndex(), submission.tintLayers(), submission.tintColor());
        output.add(new BlockModelApi.Quad(
                textureLocation,
                getVertex(submission, quad, 0, color),
                getVertex(submission, quad, 1, color),
                getVertex(submission, quad, 2, color),
                getVertex(submission, quad, 3, color)));
    }

    private BlockModelApi.Vertex getVertex(ExtendedBlockModelSubmit submission, QuadView quad, int index, int color) {
        Vector3f pos = submission.pose().pose().transformPosition(quad.x(index), quad.y(index), quad.z(index), new Vector3f());
        int vertexColor = ColorUtils.Int.multiply(quad.color(index), color);
        return new BlockModelApi.Vertex(pos.x(), pos.y(), pos.z(), vertexColor, quad.u(index), quad.v(index));
    }
}