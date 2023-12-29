package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.platform.Window;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.render.gl.ThickLinesProgram;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL30;

public class ThickLineRenderer implements LineRenderer {

    private ThickLinesProgram program;
    private RenderWorldLastEvent event;
    private Vec3 view;

    @Override
    public void begin(RenderWorldLastEvent event, boolean depthTest) {
        this.event = event;
        this.view = event.getCamera().getPosition();

        if (program == null) {
            program = new ThickLinesProgram();
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
        for (int i = 0; i < 3; i++) {
            program.buffer.add((float) (x1 - view.x));
            program.buffer.add((float) (y1 - view.y));
            program.buffer.add((float) (z1 - view.z));
        }
        for (int i = 0; i < 3; i++) {
            program.buffer.add((float) (x2 - view.x));
            program.buffer.add((float) (y2 - view.y));
            program.buffer.add((float) (z2 - view.z));
        }
    }

    @Override
    public void end() {
        // set settings
        GL30.glEnable(GL30.GL_BLEND);
        GL30.glDisable(GL30.GL_DEPTH_TEST);

        // draw with shader program
        //Matrix4f mvp = new Matrix4f(event.getPoseMatrix()).mul(event.getProjectionMatrix());
        Matrix4f mvp = new Matrix4f(event.getProjectionMatrix()).mul(event.getPoseMatrix());
        Window window = Minecraft.getInstance().getWindow();
        Vector2f resolution = new Vector2f(window.getWidth(), window.getHeight());
        program.draw(mvp, resolution, 10);

        // reset settings
        GL30.glDisable(GL30.GL_BLEND);
        GL30.glEnable(GL30.GL_DEPTH_TEST);

        // reset renderer state
        this.event = null;
        this.view = null;
    }

    @Override
    public void close() {
        program.delete();
    }
}