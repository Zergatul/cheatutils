package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;

import java.awt.Color;
import java.nio.ByteBuffer;

public class InstancedCubeLineRenderer implements AutoCloseable {

    private static final int RECORD_SIZE = 20;
    private static final String SHADER_ROOT = "assets/cheatutils/shaders/";

    private final DynamicInstancedBuffer buffer = new DynamicInstancedBuffer(RECORD_SIZE);
    private final float[] matrix = new float[16];
    private ShaderProgram program;
    private int mvpUniform;
    private int viewportUniform;

    public void begin() {
        buffer.begin();
    }

    public void cube(float x, float y, float z, Color color, float width) {
        ByteBuffer record = buffer.record();
        record.putFloat(x);
        record.putFloat(y);
        record.putFloat(z);
        putColor(record, color);
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
        uploadCommonUniforms(event);
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
                SHADER_ROOT + "instanced-cube-lines.vsh",
                SHADER_ROOT + "instanced-lines.fsh",
                "inOrigin", "inColor", "inLineWidth");
        mvpUniform = program.getUniform("MVP");
        viewportUniform = program.getUniform("ViewportSize");

        buffer.initialize();
        buffer.attribute(0, 3, GL11.GL_FLOAT, false, 0);
        buffer.attribute(1, 4, GL11.GL_UNSIGNED_BYTE, true, 12);
        buffer.attribute(2, 1, GL11.GL_FLOAT, false, 16);
        buffer.finishInitialization();
    }

    private void uploadCommonUniforms(RenderWorldLastEvent event) {
        Matrix4f mvp = event.getMvpMatrix();
        GL20.glUniformMatrix4fv(mvpUniform, false, mvp.get(matrix));
        GL20.glUniform2f(
                viewportUniform,
                Minecraft.getInstance().getWindow().getWidth(),
                Minecraft.getInstance().getWindow().getHeight());
    }

    static void putColor(ByteBuffer buffer, Color color) {
        buffer.put((byte) color.getRed());
        buffer.put((byte) color.getGreen());
        buffer.put((byte) color.getBlue());
        buffer.put((byte) color.getAlpha());
    }
}