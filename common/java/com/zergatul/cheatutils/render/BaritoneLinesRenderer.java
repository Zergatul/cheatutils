package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class BaritoneLinesRenderer extends LineRenderer {

    @Override
    public void begin(Vec3 view, PoseStack.Pose pose, Matrix4f projectionMatrix, boolean depthTest) {
        super.begin(view, pose, projectionMatrix, depthTest);

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        RenderSystem.lineWidth(1f);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        if (depthTest) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);

        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
    }

    @Override
    public void line(
            double x1, double y1, double z1, float r1, float g1, float b1, float a1,
            double x2, double y2, double z2, float r2, float g2, float b2, float a2
    ) {
        x1 -= view.x;
        y1 -= view.y;
        z1 -= view.z;
        x2 -= view.x;
        y2 -= view.y;
        z2 -= view.z;

        final double dx = x2 - x1;
        final double dy = y2 - y1;
        final double dz = z2 - z1;

        final double invMag = 1.0 / Math.sqrt(dx * dx + dy * dy + dz * dz);
        final float nx = (float) (dx * invMag);
        final float ny = (float) (dy * invMag);
        final float nz = (float) (dz * invMag);

        buffer
                .vertex(modelViewMatrix, (float)x1, (float)y1, (float)z1)
                .color(r1, g1, b1, a1)
                .normal(normalMatrix, nx, ny, nz)
                .endVertex();

        buffer
                .vertex(modelViewMatrix, (float)x2, (float)y2, (float)z2)
                .color(r2, g2, b2, a2)
                .normal(normalMatrix, nx, ny, nz)
                .endVertex();
    }

    @Override
    public void end() {
        Tesselator.getInstance().end();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();

        super.end();
    }
}