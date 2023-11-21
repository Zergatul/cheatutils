package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.render.*;

public class RenderUtilities {

    public static final RenderUtilities instance = new RenderUtilities();

    private LineRenderer lineRenderer =
        new FastLineRenderer();
        //new DebugLinesLineRenderer();
    private BlockOverlayRenderer blockOverlayRenderer = new BlockOverlayRenderer();
    private EntityOverlayRenderer entityOverlayRenderer = new EntityOverlayRenderer();
    private EntityOutlineRenderer entityOutlineRenderer = new EntityOutlineRenderer();

    private RenderUtilities() {
        Events.WindowResize.add(this::onWindowResize);
    }

    public LineRenderer getLineRenderer() {
        return lineRenderer;
    }

    public BlockOverlayRenderer getBlockOverlayRenderer() {
        return blockOverlayRenderer;
    }

    public EntityOverlayRenderer getEntityOverlayRenderer() {
        return entityOverlayRenderer;
    }

    public EntityOutlineRenderer getEntityOutlineRenderer() {
        return entityOutlineRenderer;
    }

    private void onWindowResize() {
        FrameBuffers.onResize();
    }
}