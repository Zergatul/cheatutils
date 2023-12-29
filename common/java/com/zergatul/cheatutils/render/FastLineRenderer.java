package com.zergatul.cheatutils.render;

import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.render.gl.EspLinesProgram;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL30;

public class FastLineRenderer implements LineRenderer {

    private EspLinesProgram program;
    private RenderWorldLastEvent event;
    private Vec3 view;
    private boolean depthTest;

    @Override
    public void begin(RenderWorldLastEvent event, boolean depthTest) {
        if (this.event != null) {
            throw new IllegalStateException("Rendered is already active");
        }

        this.event = event;
        this.view = event.getCamera().getPosition();
        this.depthTest = depthTest;

        if (program == null) {
            program = new EspLinesProgram();
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
        vertex(x1, y1, z1, r1, g1, b1, a1);
        vertex(x2, y2, z2, r2, g2, b2, a2);
    }

    @Override
    public void end() {
        // set line settings
        GL30.glEnable(GL30.GL_BLEND);
        GL30.glBlendFuncSeparate(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA, GL30.GL_ONE, GL30.GL_ZERO);
        GL30.glEnable(GL30.GL_LINE_SMOOTH);
        if (depthTest) {
            GL30.glEnable(GL30.GL_DEPTH_TEST);
        } else {
            GL30.glDisable(GL30.GL_DEPTH_TEST);
        }

        // draw with shader program
        program.draw(event.getPoseMatrix(), event.getProjectionMatrix());

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

    private void vertex(double x, double y, double z, float r, float g, float b, float a) {
        program.buffer.add((float) (x - view.x));
        program.buffer.add((float) (y - view.y));
        program.buffer.add((float) (z - view.z));
        program.buffer.add(r);
        program.buffer.add(g);
        program.buffer.add(b);
        program.buffer.add(a);
    }
}