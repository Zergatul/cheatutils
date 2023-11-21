package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.zergatul.cheatutils.utils.SharedVertexBuffer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class DebugLinesLineRenderer extends LineRendererBase {

    @Override
    public void begin(Vec3 view, PoseStack.Pose pose, Matrix4f projectionMatrix, boolean depthTest) {
        super.begin(view, pose, projectionMatrix, depthTest);

        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
    }

    @Override
    public void line(
            double x1, double y1, double z1, float r1, float g1, float b1, float a1,
            double x2, double y2, double z2, float r2, float g2, float b2, float a2
    ) {
        vertex(x1, y1, z1, r1, g1, b1, a1);
        vertex(x2, y2, z2, r2, g2, b2, a2);
    }

    @Override
    public void end() {
        SharedVertexBuffer.instance.bind();
        SharedVertexBuffer.instance.upload(buffer.end());

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (depthTest) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        SharedVertexBuffer.instance.drawWithShader(this.modelViewMatrix, this.projectionMatrix, GameRenderer.getPositionColorShader());
        VertexBuffer.unbind();

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();

        super.end();
    }

    private void vertex(double x, double y, double z, float r, float g, float b, float a) {
        buffer.vertex(x - view.x, y - view.y, z - view.z).color(r, g, b, a).endVertex();
    }
}