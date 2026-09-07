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

public class EspCuboidLineRenderer implements AutoCloseable {

    public static final EspCuboidLineRenderer INSTANCE = new EspCuboidLineRenderer();

    private final DynamicInstancedBuffer buffer = new DynamicInstancedBuffer(32);
    private final FloatBuffer matrix = BufferUtils.createFloatBuffer(16);
    private ShaderProgram program;
    private int mvpUniform;
    private int viewportUniform;

    private EspCuboidLineRenderer() {}

    public void begin() {
        buffer.begin();
    }

    public void cuboid(
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            int color,
            float width
    ) {
        ByteBuffer record = buffer.record();
        record.putFloat(x1);
        record.putFloat(y1);
        record.putFloat(z1);
        record.putFloat(x2 - x1);
        record.putFloat(y2 - y1);
        record.putFloat(z2 - z1);
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
                Constants.SHADER_ROOT + "instanced-cuboid-lines.vsh",
                Constants.SHADER_ROOT + "instanced-lines.fsh",
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