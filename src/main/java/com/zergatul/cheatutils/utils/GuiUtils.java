package com.zergatul.cheatutils.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class GuiUtils {

    public static void fill(MatrixStack poseStack, int x1, int y1, int x2, int y2, int color) {
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

        Matrix4f matrix = poseStack.peek().getPositionMatrix();

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        BufferBuilder bufferbuilder = RenderSystem.renderThreadTesselator().getBuffer();
        RenderSystem.enableBlend();
        //RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferbuilder.vertex(matrix, (float)x1, (float)y2, 0.0F).color(f, f1, f2, f3).next();
        bufferbuilder.vertex(matrix, (float)x2, (float)y2, 0.0F).color(f, f1, f2, f3).next();
        bufferbuilder.vertex(matrix, (float)x2, (float)y1, 0.0F).color(f, f1, f2, f3).next();
        bufferbuilder.vertex(matrix, (float)x1, (float)y1, 0.0F).color(f, f1, f2, f3).next();
        BufferRenderer.drawWithGlobalProgram(bufferbuilder.end());
        //RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    public static void drawTexture(Matrix4f matrix, float x, float y, float width, float height, float z, int texX, int texY, int texWidth, int texHeight, int texSizeX, int texSizeY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        BufferBuilder bufferBuilder = RenderSystem.renderThreadTesselator().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix, x, y, z).texture(1F * texX / texSizeX, 1F * texY / texSizeY).next();
        bufferBuilder.vertex(matrix, x, y + height, z).texture(1F * texX / texSizeX, 1F * (texY + texHeight) / texSizeY).next();
        bufferBuilder.vertex(matrix, x + width, y + height, z).texture(1F * (texX + texWidth) / texSizeX, 1F * (texY + texHeight) / texSizeY).next();
        bufferBuilder.vertex(matrix, x + width, y, z).texture(1F * (texX + texWidth) / texSizeX, 1F * texY / texSizeY).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }
}