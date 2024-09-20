package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.platform.Window;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.render.gl.EspTrianglesProgram;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL30;

public class ThickLineRenderer implements LineRenderer {

    private EspTrianglesProgram program;
    private RenderWorldLastEvent event;
    private Vec3 view;

    @Override
    public void begin(RenderWorldLastEvent event, boolean depthTest) {
        if (depthTest) {
            throw new IllegalStateException("Depth test is not supported.");
        }
        if (this.event != null) {
            throw new IllegalStateException("Rendered is already active");
        }

        this.event = event;
        this.view = event.getCamera().getPosition();

        if (program == null) {
            program = new EspTrianglesProgram();
        }

        program.buffer.clear();
    }

    @Override
    public void line(
            double x1, double y1, double z1,
            float r1, float g1, float b1, float a1,
            double x2, double y2, double z2,
            float r2, float g2, float b2, float a2
    ) {
        var v1 = new Vector4f((float) (x1 - view.x), (float) (y1 - view.y), (float) (z1 - view.z), 1).mul(event.getMvp());
        var v2 = new Vector4f((float) (x2 - view.x), (float) (y2 - view.y), (float) (z2 - view.z), 1).mul(event.getMvp());
        if (v1.w < 0 && v2.w < 0) {
            return;
        }

        if (v1.w < 0 || v2.w < 0) {
            // clipping
            float t = (v1.w - 1e-3F) / (v1.w - v2.w);
            if (v1.w <= 0) {
                v1.lerp(v2, t);
            } else {
                v2.lerp(v1, 1 - t);
            }
        }

        Window window = Minecraft.getInstance().getWindow();
        var rect = new Vector4f[4];
        createRect(v1, v2, 5, window.getWidth(), window.getHeight(), rect);

        point(rect[0], r1, g1, b1, a1);
        point(rect[1], r1, g1, b1, a1);
        point(rect[3], r1, g1, b1, a1);

        point(rect[0], r1, g1, b1, a1);
        point(rect[3], r1, g1, b1, a1);
        point(rect[2], r1, g1, b1, a1);
    }

    @Override
    public void end() {
        // set line settings
        GL30.glEnable(GL30.GL_BLEND);
        GL30.glBlendFuncSeparate(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA, GL30.GL_ONE, GL30.GL_ZERO);
        GL30.glDisable(GL30.GL_DEPTH_TEST);
        GL30.glDisable(GL30.GL_CULL_FACE);

        // draw with shader program
        program.draw();

        // reset settings
        GL30.glDisable(GL30.GL_BLEND);
        GL30.glEnable(GL30.GL_DEPTH_TEST);
        GL30.glEnable(GL30.GL_CULL_FACE);

        // reset renderer state
        this.event = null;
        this.view = null;
    }

    @Override
    public void close() {
        program.delete();
    }

    private void createRect(Vector4f v0_clip, Vector4f v1_clip, float lineWidth, int viewportWidth, int viewportHeight, Vector4f[] rect) {
        // Step 1: Convert clip-space coordinates to NDC (Normalized Device Coordinates)
        Vector3f v0_ndc = new Vector3f(v0_clip.x / v0_clip.w, v0_clip.y / v0_clip.w, v0_clip.z / v0_clip.w);
        Vector3f v1_ndc = new Vector3f(v1_clip.x / v1_clip.w, v1_clip.y / v1_clip.w, v1_clip.z / v1_clip.w);

        // Step 2: Convert NDC to screen coordinates
        float x0_screen = (v0_ndc.x + 1.0f) * 0.5f * viewportWidth;
        float y0_screen = (v0_ndc.y + 1.0f) * 0.5f * viewportHeight;

        float x1_screen = (v1_ndc.x + 1.0f) * 0.5f * viewportWidth;
        float y1_screen = (v1_ndc.y + 1.0f) * 0.5f * viewportHeight;

        // Step 3: Calculate the direction vector of the line in screen space
        float dx = x1_screen - x0_screen;
        float dy = y1_screen - y0_screen;

        // Calculate the length to normalize the perpendicular vector
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length == 0.0f) {
            // Avoid division by zero for zero-length lines
            return;
        }

        // Step 4: Compute the normalized perpendicular vector
        float px = -dy / length;
        float py = dx / length;

        // Step 5: Scale the perpendicular vector by half the line width to get the offset
        float offsetX = px * (lineWidth / 2.0f);
        float offsetY = py * (lineWidth / 2.0f);

        // Step 6: Calculate the four corner points of the rectangle in screen space
        float x0a = x0_screen + offsetX;
        float y0a = y0_screen + offsetY;

        float x0b = x0_screen - offsetX;
        float y0b = y0_screen - offsetY;

        float x1a = x1_screen + offsetX;
        float y1a = y1_screen + offsetY;

        float x1b = x1_screen - offsetX;
        float y1b = y1_screen - offsetY;

        // Step 7: Convert the rectangle's corner points back to NDC coordinates
        float x0a_ndc = (x0a / (viewportWidth * 0.5f)) - 1.0f;
        float y0a_ndc = (y0a / (viewportHeight * 0.5f)) - 1.0f;

        float x0b_ndc = (x0b / (viewportWidth * 0.5f)) - 1.0f;
        float y0b_ndc = (y0b / (viewportHeight * 0.5f)) - 1.0f;

        float x1a_ndc = (x1a / (viewportWidth * 0.5f)) - 1.0f;
        float y1a_ndc = (y1a / (viewportHeight * 0.5f)) - 1.0f;

        float x1b_ndc = (x1b / (viewportWidth * 0.5f)) - 1.0f;
        float y1b_ndc = (y1b / (viewportHeight * 0.5f)) - 1.0f;

        // Step 8: Reconstruct clip-space positions for the rectangle's corners
        float z0 = v0_ndc.z;
        float z1 = v1_ndc.z;
        float w0 = v0_clip.w;
        float w1 = v1_clip.w;

        Vector4f p0a_clip = new Vector4f(x0a_ndc * w0, y0a_ndc * w0, z0 * w0, w0);
        Vector4f p0b_clip = new Vector4f(x0b_ndc * w0, y0b_ndc * w0, z0 * w0, w0);

        Vector4f p1a_clip = new Vector4f(x1a_ndc * w1, y1a_ndc * w1, z1 * w1, w1);
        Vector4f p1b_clip = new Vector4f(x1b_ndc * w1, y1b_ndc * w1, z1 * w1, w1);

        // Step 9: Store the rectangle's corner vertices in the output array
        rect[0] = p0a_clip;
        rect[1] = p0b_clip;
        rect[2] = p1a_clip;
        rect[3] = p1b_clip;
    }

    private void point(Vector4f v, float r, float g, float b, float a) {
        program.buffer.add(v.x / v.w);
        program.buffer.add(v.y / v.w);
        program.buffer.add(v.z / v.w);
        program.buffer.add(r);
        program.buffer.add(g);
        program.buffer.add(b);
        program.buffer.add(a);
    }
}