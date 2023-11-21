package com.zergatul.cheatutils.render;

import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;

public interface LineRenderer {

    void begin(RenderWorldLastEvent event, boolean depthTest);

    void line(
            double x1, double y1, double z1,
            float r1, float g1, float b1, float a1,
            double x2, double y2, double z2,
            float r2, float g2, float b2, float a2);

    void end();

    void close();

    default void line(
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            float r, float g, float b, float a
    ) {
        line(x1, y1, z1, r, g, b, a, x2, y2, z2, r, g, b, a);
    }

    default void cuboid(
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            float r, float g, float b, float a
    ) {
        line(x1, y1, z1, x1, y1, z2, r, g, b, a);
        line(x1, y1, z2, x2, y1, z2, r, g, b, a);
        line(x2, y1, z2, x2, y1, z1, r, g, b, a);
        line(x2, y1, z1, x1, y1, z1, r, g, b, a);

        line(x1, y2, z1, x1, y2, z2, r, g, b, a);
        line(x1, y2, z2, x2, y2, z2, r, g, b, a);
        line(x2, y2, z2, x2, y2, z1, r, g, b, a);
        line(x2, y2, z1, x1, y2, z1, r, g, b, a);

        line(x1, y1, z1, x1, y2, z1, r, g, b, a);
        line(x1, y1, z2, x1, y2, z2, r, g, b, a);
        line(x2, y1, z2, x2, y2, z2, r, g, b, a);
        line(x2, y1, z1, x2, y2, z1, r, g, b, a);
    }
}