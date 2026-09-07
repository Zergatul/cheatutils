package com.zergatul.cheatutils.render;

import com.zergatul.cheatutils.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class EspCubeLineRender implements AutoCloseable {

    public static final EspCubeLineRender INSTANCE = new EspCubeLineRender();

    private final DynamicInstancedBuffer buffer = new DynamicInstancedBuffer(20);
    private final FloatBuffer matrix = BufferUtils.createFloatBuffer(16);
    private ShaderProgram program;
    private int mvpUniform;
    private int viewportUniform;

    private EspCubeLineRender() {}

    public void begin() {
        buffer.begin();
    }

    public void cube(float x, float y, float z, int color, float width) {
        ByteBuffer record = buffer.record();
        record.putFloat(x);
        record.putFloat(y);
        record.putFloat(z);
        record.put((byte) (color >>> 16));
        record.put((byte) (color >>> 8));
        record.put((byte) color);
        record.put((byte) (color >>> 24));
        record.putFloat(width);
    }

    public void end(Matrix4f mvp) {
        if (buffer.getInstances() == 0) {
            return;
        }
        ensureInitialized();
        buffer.uploadAndBind();

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableDepth();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();

        GL20.glUseProgram(program.getId());
        matrix.clear();
        mvp.store(matrix);
        matrix.flip();
        GL20.glUniformMatrix4(mvpUniform, false, matrix);
        Minecraft mc = Minecraft.getMinecraft();
        GL20.glUniform2f(viewportUniform, mc.displayWidth, mc.displayHeight);
        GL31.glDrawArraysInstanced(GL11.GL_TRIANGLES, 0, 72, buffer.getInstances());
        GL20.glUseProgram(0);
        buffer.unbind();

        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
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
                Constants.SHADER_ROOT + "instanced-cube-lines.vsh",
                Constants.SHADER_ROOT + "instanced-lines.fsh",
                "inOrigin", "inColor", "inLineWidth");
        mvpUniform = program.getUniform("MVP");
        viewportUniform = program.getUniform("ViewportSize");

        buffer.initialize();
        buffer.attribute(0, 3, GL11.GL_FLOAT, false, 0);
        buffer.attribute(1, 4, GL11.GL_UNSIGNED_BYTE, true, 12);
        buffer.attribute(2, 1, GL11.GL_FLOAT, false, 16);
        buffer.finishInitialization();
    }
}