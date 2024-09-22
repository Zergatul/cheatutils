package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.render.*;

public class RenderUtilities {

    public static final RenderUtilities instance = new RenderUtilities();

    private final LineRenderer lineRenderer = new FastLineRenderer();
    private final ThickLineRenderer thickLineRenderer = new NoAAThickLineRenderer();
    private final GroupLineRenderer groupLineRenderer = new FastGroupLineRenderer();
    private final GroupThickLineRenderer groupThickLineRenderer = new QuadAAGroupThickLineRenderer();
    private final BlockOverlayRenderer blockOverlayRenderer = new BlockOverlayRenderer();
    private final EntityOverlayRenderer entityOverlayRenderer = new EntityOverlayRenderer();
    private final EntityOutlineRenderer entityOutlineRenderer = new EntityOutlineRenderer();

    private RenderUtilities() {
        Events.WindowResize.add(this::onWindowResize);
    }

    public LineRenderer getLineRenderer() {
        return lineRenderer;
    }

    public ThickLineRenderer getThickLineRenderer() {
        return thickLineRenderer;
    }

    public GroupLineRenderer getGroupLineRenderer() {
        return groupLineRenderer;
    }

    public GroupThickLineRenderer getGroupThickLineRenderer() {
        return groupThickLineRenderer;
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