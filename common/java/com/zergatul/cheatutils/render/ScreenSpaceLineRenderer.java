package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.utils.SharedVertexBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class ScreenSpaceLineRenderer {

    private static final float FEATHER = 1.25f;
    private static final float MIN_CLIP_W = 0.0001f;

    private final BufferBuilder buffer = new BufferBuilder(4096);
    private final Vector4f clip1 = new Vector4f();
    private final Vector4f clip2 = new Vector4f();
    private final Point[] points1 = createPoints();
    private final Point[] points2 = createPoints();

    private RenderWorldLastEvent event;
    private Vec3 view;
    private int viewportWidth;
    private int viewportHeight;

    public void begin(RenderWorldLastEvent event) {
        if (this.event != null) {
            throw new IllegalStateException("Renderer is already active");
        }

        this.event = event;
        this.view = event.getCamera().getPosition();
        this.viewportWidth = Minecraft.getInstance().getWindow().getWidth();
        this.viewportHeight = Minecraft.getInstance().getWindow().getHeight();
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
    }

    public void cuboid(
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            float width,
            float r, float g, float b, float a
    ) {
        line(x1, y1, z1, x1, y1, z2, width, r, g, b, a);
        line(x1, y1, z2, x2, y1, z2, width, r, g, b, a);
        line(x2, y1, z2, x2, y1, z1, width, r, g, b, a);
        line(x2, y1, z1, x1, y1, z1, width, r, g, b, a);

        line(x1, y2, z1, x1, y2, z2, width, r, g, b, a);
        line(x1, y2, z2, x2, y2, z2, width, r, g, b, a);
        line(x2, y2, z2, x2, y2, z1, width, r, g, b, a);
        line(x2, y2, z1, x1, y2, z1, width, r, g, b, a);

        line(x1, y1, z1, x1, y2, z1, width, r, g, b, a);
        line(x1, y1, z2, x1, y2, z2, width, r, g, b, a);
        line(x2, y1, z2, x2, y2, z2, width, r, g, b, a);
        line(x2, y1, z1, x2, y2, z1, width, r, g, b, a);
    }

    public void line(
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            float width,
            float r, float g, float b, float a
    ) {
        if (event == null) {
            throw new IllegalStateException("Renderer is not active");
        }

        Matrix4f mvp = event.getMvpMatrix();
        clip1.set((float) (x1 - view.x), (float) (y1 - view.y), (float) (z1 - view.z), 1);
        clip2.set((float) (x2 - view.x), (float) (y2 - view.y), (float) (z2 - view.z), 1);
        mvp.transform(clip1);
        mvp.transform(clip2);

        if (!clipToView()) {
            return;
        }

        float ndcX1 = clip1.x / clip1.w;
        float ndcY1 = clip1.y / clip1.w;
        float ndcZ1 = clip1.z / clip1.w;
        float ndcX2 = clip2.x / clip2.w;
        float ndcY2 = clip2.y / clip2.w;
        float ndcZ2 = clip2.z / clip2.w;

        float screenX1 = (ndcX1 + 1) * 0.5f * viewportWidth;
        float screenY1 = (ndcY1 + 1) * 0.5f * viewportHeight;
        float screenX2 = (ndcX2 + 1) * 0.5f * viewportWidth;
        float screenY2 = (ndcY2 + 1) * 0.5f * viewportHeight;

        float dx = screenX2 - screenX1;
        float dy = screenY2 - screenY1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length == 0 || !Float.isFinite(length)) {
            return;
        }

        float perpendicularX = -dy / length;
        float perpendicularY = dx / length;
        float halfWidth = width * 0.5f;
        setPoints(points1, screenX1, screenY1, ndcZ1, perpendicularX, perpendicularY, halfWidth);
        setPoints(points2, screenX2, screenY2, ndcZ2, perpendicularX, perpendicularY, halfWidth);

        quad(points1[0], 0, points2[0], 0, points2[1], a, points1[1], a, r, g, b);
        quad(points1[1], a, points2[1], a, points2[2], a, points1[2], a, r, g, b);
        quad(points1[2], a, points2[2], a, points2[3], 0, points1[3], 0, r, g, b);
    }

    public void end() {
        if (event == null) {
            throw new IllegalStateException("Renderer is not active");
        }

        BufferBuilder.RenderedBuffer renderedBuffer = buffer.endOrDiscardIfEmpty();
        if (renderedBuffer == null) {
            reset();
            return;
        }

        SharedVertexBuffer.instance.bind();
        SharedVertexBuffer.instance.upload(renderedBuffer);

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        SharedVertexBuffer.instance.drawWithShader(
                new Matrix4f(),
                new Matrix4f(),
                GameRenderer.getPositionColorShader());
        VertexBuffer.unbind();

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        reset();
    }

    private boolean clipToView() {
        if (clip1.w <= MIN_CLIP_W && clip2.w <= MIN_CLIP_W) {
            return false;
        }
        if (clip1.w <= MIN_CLIP_W) {
            float t = (MIN_CLIP_W - clip1.w) / (clip2.w - clip1.w);
            clip1.lerp(clip2, t);
        } else if (clip2.w <= MIN_CLIP_W) {
            float t = (MIN_CLIP_W - clip2.w) / (clip1.w - clip2.w);
            clip2.lerp(clip1, t);
        }
        return true;
    }

    private void setPoints(
            Point[] points,
            float x, float y, float z,
            float perpendicularX, float perpendicularY,
            float halfWidth
    ) {
        float outer = halfWidth + FEATHER * 0.5f;
        float inner = Math.max(0, halfWidth - FEATHER * 0.5f);
        setPoint(points[0], x, y, z, perpendicularX, perpendicularY, outer);
        setPoint(points[1], x, y, z, perpendicularX, perpendicularY, inner);
        setPoint(points[2], x, y, z, perpendicularX, perpendicularY, -inner);
        setPoint(points[3], x, y, z, perpendicularX, perpendicularY, -outer);
    }

    private void setPoint(
            Point point,
            float x, float y, float z,
            float perpendicularX, float perpendicularY,
            float offset
    ) {
        float screenX = x + perpendicularX * offset;
        float screenY = y + perpendicularY * offset;
        point.x = screenX / (viewportWidth * 0.5f) - 1;
        point.y = screenY / (viewportHeight * 0.5f) - 1;
        point.z = z;
    }

    private void quad(
            Point p1, float a1,
            Point p2, float a2,
            Point p3, float a3,
            Point p4, float a4,
            float r, float g, float b
    ) {
        vertex(p1, r, g, b, a1);
        vertex(p2, r, g, b, a2);
        vertex(p3, r, g, b, a3);
        vertex(p1, r, g, b, a1);
        vertex(p3, r, g, b, a3);
        vertex(p4, r, g, b, a4);
    }

    private void vertex(Point point, float r, float g, float b, float a) {
        buffer.vertex(point.x, point.y, point.z).color(r, g, b, a).endVertex();
    }

    private void reset() {
        event = null;
        view = null;
    }

    private static Point[] createPoints() {
        return new Point[] { new Point(), new Point(), new Point(), new Point() };
    }

    private static class Point {
        private float x;
        private float y;
        private float z;
    }
}