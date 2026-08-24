package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.render.BlockOverlayRenderer;
import com.zergatul.cheatutils.render.DebugLinesLineRenderer;
import com.zergatul.cheatutils.render.LineRenderer;
import com.zergatul.cheatutils.render.ScreenSpaceLineRenderer;

public class RenderUtilities {

    public static final RenderUtilities instance = new RenderUtilities();

    private final LineRenderer lineRenderer = new DebugLinesLineRenderer();
    private final ScreenSpaceLineRenderer screenSpaceLineRenderer = new ScreenSpaceLineRenderer();
    private final BlockOverlayRenderer blockOverlayRenderer = new BlockOverlayRenderer();

    private RenderUtilities() {

    }

    public LineRenderer getLineRenderer() {
        return lineRenderer;
    }

    public ScreenSpaceLineRenderer getScreenSpaceLineRenderer() {
        return screenSpaceLineRenderer;
    }

    public BlockOverlayRenderer getBlockOverlayRenderer() {
        return blockOverlayRenderer;
    }
}