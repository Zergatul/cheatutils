package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public abstract class LineRendererBase implements LineRenderer {

    protected BufferBuilder buffer;
    protected Vec3 view;
    protected Matrix4f pose;
    protected Matrix4f projection;
    protected boolean depthTest;

    public void begin(RenderWorldLastEvent event, boolean depthTest) {
        begin(event.getCamera().getPosition(), event.getPose(), event.getProjection(), depthTest);
    }

    public void begin(Vec3 view, Matrix4f pose, Matrix4f projection, boolean depthTest) {
        if (buffer != null) {
            throw new IllegalStateException("Rendered is already active");
        }

        this.view = view;
        this.pose = pose;
        this.projection = projection;
        this.depthTest = depthTest;
        buffer = Tesselator.getInstance().getBuilder();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public void line(
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            float r, float g, float b, float a
    ) {
        line(x1, y1, z1, r, g, b, a, x2, y2, z2, r, g, b, a);
    }

    public void cuboid(
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

    public abstract void line(
            double x1, double y1, double z1,
            float r1, float g1, float b1, float a1,
            double x2, double y2, double z2,
            float r2, float g2, float b2, float a2);

    public void end() {
        this.view = null;
        this.pose = null;
        this.projection = null;
        this.buffer = null;
    }

    public void close() {

    }
}