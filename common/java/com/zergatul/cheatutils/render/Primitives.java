package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.zergatul.cheatutils.utils.SharedVertexBuffer;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class Primitives {

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

    public static void fill(PoseStack poseStack, int x1, int y1, int x2, int y2, int color) {
        if (x1 < x2) {
            int i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            int j = y1;
            y1 = y2;
            y2 = j;
        }

        Matrix4f matrix = poseStack.last().pose();

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(matrix, (float)x1, (float)y2, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(matrix, (float)x2, (float)y2, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(matrix, (float)x2, (float)y1, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(matrix, (float)x1, (float)y1, 0.0F).color(f, f1, f2, f3).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void fill(PoseStack poseStack, double x1, double y1, double x2, double y2, int color) {
        if (x1 < x2) {
            double buf = x1;
            x1 = x2;
            x2 = buf;
        }

        if (y1 < y2) {
            double buf = y1;
            y1 = y2;
            y2 = buf;
        }

        Matrix4f matrix = poseStack.last().pose();

        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(matrix, (float)x1, (float)y2, 0.0F).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(matrix, (float)x2, (float)y2, 0.0F).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(matrix, (float)x2, (float)y1, 0.0F).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(matrix, (float)x1, (float)y1, 0.0F).color(r, g, b, a).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    // copy from
    // Lnet/minecraft/client/renderer/entity/ItemRenderer;fillRect(Lcom/mojang/blaze3d/vertex/BufferBuilder;IIIIIIII)V
    public static void fillRect(BufferBuilder p_115153_, double p_115154_, double p_115155_, int p_115156_, int p_115157_, int p_115158_, int p_115159_, int p_115160_, int p_115161_) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        p_115153_.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        p_115153_.vertex((double)(p_115154_ + 0), (double)(p_115155_ + 0), 0.0D).color(p_115158_, p_115159_, p_115160_, p_115161_).endVertex();
        p_115153_.vertex((double)(p_115154_ + 0), (double)(p_115155_ + p_115157_), 0.0D).color(p_115158_, p_115159_, p_115160_, p_115161_).endVertex();
        p_115153_.vertex((double)(p_115154_ + p_115156_), (double)(p_115155_ + p_115157_), 0.0D).color(p_115158_, p_115159_, p_115160_, p_115161_).endVertex();
        p_115153_.vertex((double)(p_115154_ + p_115156_), (double)(p_115155_ + 0), 0.0D).color(p_115158_, p_115159_, p_115160_, p_115161_).endVertex();
        BufferUploader.drawWithShader(p_115153_.end());
    }

    public static void drawTexture(Matrix4f matrix, float x, float y, float width, float height, float z, int texX, int texY, int texWidth, int texHeight, int texSizeX, int texSizeY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix, x, y, z).uv(1F * texX / texSizeX, 1F * texY / texSizeY).endVertex();
        bufferBuilder.vertex(matrix, x, y + height, z).uv(1F * texX / texSizeX, 1F * (texY + texHeight) / texSizeY).endVertex();
        bufferBuilder.vertex(matrix, x + width, y + height, z).uv(1F * (texX + texWidth) / texSizeX, 1F * (texY + texHeight) / texSizeY).endVertex();
        bufferBuilder.vertex(matrix, x + width, y, z).uv(1F * (texX + texWidth) / texSizeX, 1F * texY / texSizeY).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }
}