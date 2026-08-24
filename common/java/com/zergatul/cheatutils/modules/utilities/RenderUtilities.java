package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.render.BlockOverlayRenderer;
import com.zergatul.cheatutils.render.DebugLinesLineRenderer;
import com.zergatul.cheatutils.render.InstancedCuboidLineRenderer;
import com.zergatul.cheatutils.render.InstancedCubeLineRenderer;
import com.zergatul.cheatutils.render.InstancedTracerRenderer;
import com.zergatul.cheatutils.render.LineRenderer;

public class RenderUtilities {

    public static final RenderUtilities instance = new RenderUtilities();

    private final LineRenderer lineRenderer = new DebugLinesLineRenderer();
    private final InstancedCubeLineRenderer instancedCubeLineRenderer = new InstancedCubeLineRenderer();
    private final InstancedCuboidLineRenderer instancedCuboidLineRenderer = new InstancedCuboidLineRenderer();
    private final InstancedTracerRenderer instancedTracerRenderer = new InstancedTracerRenderer();
    private final BlockOverlayRenderer blockOverlayRenderer = new BlockOverlayRenderer();

    private RenderUtilities() {

    }

    public LineRenderer getLineRenderer() {
        return lineRenderer;
    }

    public InstancedCubeLineRenderer getInstancedCubeLineRenderer() {
        return instancedCubeLineRenderer;
    }

    public InstancedCuboidLineRenderer getInstancedCuboidLineRenderer() {
        return instancedCuboidLineRenderer;
    }

    public InstancedTracerRenderer getInstancedTracerRenderer() {
        return instancedTracerRenderer;
    }

    public BlockOverlayRenderer getBlockOverlayRenderer() {
        return blockOverlayRenderer;
    }
}