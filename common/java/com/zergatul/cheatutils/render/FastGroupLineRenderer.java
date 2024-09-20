package com.zergatul.cheatutils.render;

import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.render.gl.EspGroupLinesProgram;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL30;

public class FastGroupLineRenderer implements GroupLineRenderer {

    private EspGroupLinesProgram program;
    private RenderWorldLastEvent event;
    private Vec3 view;

    @Override
    public void begin(RenderWorldLastEvent event) {
        if (this.event != null) {
            throw new IllegalStateException("Rendered is already active");
        }

        this.event = event;
        this.view = event.getCamera().getPosition();

        if (program == null) {
            program = new EspGroupLinesProgram();
        }

        program.buffer.clear();
    }

    @Override
    public void line(double x1, double y1, double z1, double x2, double y2, double z2) {
        vertex(x1, y1, z1);
        vertex(x2, y2, z2);
    }

    @Override
    public void end(float r, float g, float b, float a) {
        // set line settings
        GL30.glEnable(GL30.GL_BLEND);
        GL30.glBlendFuncSeparate(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA, GL30.GL_ONE, GL30.GL_ZERO);
        GL30.glEnable(GL30.GL_LINE_SMOOTH);
        GL30.glDisable(GL30.GL_DEPTH_TEST);

        // draw with shader program
        program.draw(event.getMvp(), r, g, b, a);

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

    private void vertex(double x, double y, double z) {
        program.buffer.add((float) (x - view.x));
        program.buffer.add((float) (y - view.y));
        program.buffer.add((float) (z - view.z));
    }
}