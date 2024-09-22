package com.zergatul.cheatutils.render;

import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;

import java.awt.*;

public interface GroupThickLineRenderer extends CuboidRenderer, SimpleLineRenderer {

    void begin(RenderWorldLastEvent event, float width);

    void end(float r, float g, float b, float a);

    void close();

    default void cuboid(
            double x1, double y1, double z1,
            double x2, double y2, double z2
    ) {
        line(x1, y1, z1, x1, y1, z2);
        line(x1, y1, z2, x2, y1, z2);
        line(x2, y1, z2, x2, y1, z1);
        line(x2, y1, z1, x1, y1, z1);

        line(x1, y2, z1, x1, y2, z2);
        line(x1, y2, z2, x2, y2, z2);
        line(x2, y2, z2, x2, y2, z1);
        line(x2, y2, z1, x1, y2, z1);

        line(x1, y1, z1, x1, y2, z1);
        line(x1, y1, z2, x1, y2, z2);
        line(x2, y1, z2, x2, y2, z2);
        line(x2, y1, z1, x2, y2, z1);
    }

    default void end(Color color) {
        end(
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                color.getAlpha() / 255f);
    }
}