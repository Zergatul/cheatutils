package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;

import java.awt.Color;
import java.nio.ByteBuffer;

public class InstancedCuboidLineRenderer implements AutoCloseable {

    private static final int RECORD_SIZE = 32;
    private static final String SHADER_ROOT = "assets/cheatutils/shaders/";

    private final DynamicInstancedBuffer buffer = new DynamicInstancedBuffer(RECORD_SIZE);
    private final float[] matrix = new float[16];
    private ShaderProgram program;
    private int mvpUniform;
    private int viewportUniform;

    public void begin() {
        buffer.begin();
    }

    public void cuboid(
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            Color color,
            float width
    ) {
        ByteBuffer record = buffer.record();
        record.putFloat(x1);
        record.putFloat(y1);
        record.putFloat(z1);
        record.putFloat(x2 - x1);
        record.putFloat(y2 - y1);
        record.putFloat(z2 - z1);
        InstancedCubeLineRenderer.putColor(record, color);
        record.putFloat(width);
    }

    public void end(RenderWorldLastEvent event) {
        if (buffer.getInstances() == 0) {
            return;
        }
        ensureInitialized();
        buffer.uploadAndBind();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        GL20.glUseProgram(program.getId());
        GL20.glUniformMatrix4fv(mvpUniform, false, event.getMvpMatrix().get(matrix));
        GL20.glUniform2f(
                viewportUniform,
                Minecraft.getInstance().getWindow().getWidth(),
                Minecraft.getInstance().getWindow().getHeight());
        GL31.glDrawArraysInstanced(GL11.GL_TRIANGLES, 0, 72, buffer.getInstances());
        GL20.glUseProgram(0);
        buffer.unbind();

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    @Override
    public void close() {
        buffer.close();
        if (program != null) {
            program.close();
            program = null;
        }
    }

    private void ensureInitialized() {
        if (program != null) {
            return;
        }
        program = new ShaderProgram(
                SHADER_ROOT + "instanced-cuboid-lines.vsh",
                SHADER_ROOT + "instanced-lines.fsh",
                "inOrigin", "inSize", "inColor", "inLineWidth");
        mvpUniform = program.getUniform("MVP");
        viewportUniform = program.getUniform("ViewportSize");

        buffer.initialize();
        buffer.attribute(0, 3, GL11.GL_FLOAT, false, 0);
        buffer.attribute(1, 3, GL11.GL_FLOAT, false, 12);
        buffer.attribute(2, 4, GL11.GL_UNSIGNED_BYTE, true, 24);
        buffer.attribute(3, 1, GL11.GL_FLOAT, false, 28);
        buffer.finishInitialization();
    }
}