package com.zergatul.cheatutils.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class GlUtils {

    public static void drawCube(
            BufferBuilder bufferBuilder,
            double x1, double y1, double z1,
            double x2, double y2, double z2) {
        bufferBuilder.vertex(x1, y1, z1).color(1f, 1f, 1f, 1f).endVertex();
        bufferBuilder.vertex(x1, y1, z2).color(1f, 1f, 1f, 1f).endVertex();
        bufferBuilder.vertex(x1, y1, z2).color(1f, 1f, 1f, 1f).endVertex();
        bufferBuilder.vertex(x2, y1, z2).color(1f, 1f, 1f, 1f).endVertex();
        bufferBuilder.vertex(x2, y1, z2).color(1f, 1f, 1f, 1f).endVertex();
        bufferBuilder.vertex(x2, y1, z1).color(1f, 1f, 1f, 1f).endVertex();
        bufferBuilder.vertex(x2, y1, z1).color(1f, 1f, 1f, 1f).endVertex();
        bufferBuilder.vertex(x1, y1, z1).color(1f, 1f, 1f, 1f).endVertex();

        bufferBuilder.vertex(x1, y2, z1).color(1f, 1f, 1f, 1f).endVertex();
        bufferBuilder.vertex(x1, y2, z2).color(1f, 1f, 1f, 1f).endVertex();
        bufferBuilder.vertex(x1, y2, z2).color(1f, 1f, 1f, 1f).endVertex();
        bufferBuilder.vertex(x2, y2, z2).color(1f, 1f, 1f, 1f).endVertex();
        bufferBuilder.vertex(x2, y2, z2).color(1f, 1f, 1f, 1f).endVertex();
        bufferBuilder.vertex(x2, y2, z1).color(1f, 1f, 1f, 1f).endVertex();
        bufferBuilder.vertex(x2, y2, z1).color(1f, 1f, 1f, 1f).endVertex();
        bufferBuilder.vertex(x1, y2, z1).color(1f, 1f, 1f, 1f).endVertex();

        bufferBuilder.vertex(x1, y1, z1).color(1f, 1f, 1f, 1f).endVertex();
        bufferBuilder.vertex(x1, y2, z1).color(1f, 1f, 1f, 1f).endVertex();
        bufferBuilder.vertex(x1, y1, z2).color(1f, 1f, 1f, 1f).endVertex();
        bufferBuilder.vertex(x1, y2, z2).color(1f, 1f, 1f, 1f).endVertex();
        bufferBuilder.vertex(x2, y1, z2).color(1f, 1f, 1f, 1f).endVertex();
        bufferBuilder.vertex(x2, y2, z2).color(1f, 1f, 1f, 1f).endVertex();
        bufferBuilder.vertex(x2, y1, z1).color(1f, 1f, 1f, 1f).endVertex();
        bufferBuilder.vertex(x2, y2, z1).color(1f, 1f, 1f, 1f).endVertex();
    }

    public static void renderLines(BufferBuilder bufferBuilder, Matrix4f pose, Matrix4f projection) {
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        SharedVertexBuffer.instance.bind();
        SharedVertexBuffer.instance.upload(bufferBuilder.end());
        SharedVertexBuffer.instance.drawWithShader(pose, projection, GameRenderer.getPositionColorShader());
        VertexBuffer.unbind();

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    }
}