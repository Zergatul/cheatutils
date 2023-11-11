package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.render.DebugLinesLineRenderer;
import com.zergatul.cheatutils.render.LineRenderer;

public class RenderUtilities {

    public static final RenderUtilities instance = new RenderUtilities();

    private LineRenderer lineRenderer = new DebugLinesLineRenderer();

    private RenderUtilities() {

    }

    public LineRenderer getLineRenderer() {
        return lineRenderer;
    }
}