package com.zergatul.cheatutils.render;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;

import java.util.ArrayList;
import java.util.List;

// TODO: just an idea
public class RenderNodeCollector {

    public static final RenderNodeCollector INSTANCE = new RenderNodeCollector();

    private final List<SimpleLineSubmit> simpleWorldLines = new ArrayList<>();
    private final List<SimpleLineSubmit> simpleOverlayLines = new ArrayList<>();
    private final List<LineWithWidthSubmit> withWidthOverlayLines = new ArrayList<>();

    private RenderNodeCollector() {
        Events.AfterRenderWorld.add(this::render, 1000);
    }

    public void submitWorldLine(
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float r, float g, float b, float a,
            float width
    ) {
        if (width == 1) {
            simpleWorldLines.add(new SimpleLineSubmit(x1, y1, z1, x2, y2, z2, r, g, b, a));
        } else {
            throw new IllegalStateException();
        }
    }

    public void submitOverlayLine(
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float r, float g, float b, float a,
            float width
    ) {
        if (width == 1) {
            simpleOverlayLines.add(new SimpleLineSubmit(x1, y1, z1, x2, y2, z2, r, g, b, a));
        } else {
            withWidthOverlayLines.add(new LineWithWidthSubmit(x1, y1, z1, x2, y2, z2, r, g, b, a, width));
        }
    }

    private void render(RenderWorldLastEvent event) {
        if (!simpleWorldLines.isEmpty()) {
            renderSimpleWorldLines();
        }
    }

    private void renderSimpleWorldLines() {
//        VertexColorLineRenderer renderer = VertexColorLineRenderer.getInstance();
//        renderer.begin();
    }

    public record SimpleLineSubmit(
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float r, float g, float b, float a) {}

    public record LineWithWidthSubmit(
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float r, float g, float b, float a,
            float width) {}
}