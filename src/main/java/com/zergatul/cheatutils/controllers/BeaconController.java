package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.zergatul.cheatutils.configs.BeaconConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.interfaces.GameRendererMixinInterface;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class BeaconController {

    public static final BeaconController instance = new BeaconController();

    private static final float MaxRange = 5000;
    private static final float ClipRange = MaxRange * 2;

    private final Minecraft mc = Minecraft.getInstance();
    private VertexBuffer vertexBuffer;

    private BeaconController() {
        RenderSystem.recordRenderCall(() -> vertexBuffer = new VertexBuffer());
    }

    @SubscribeEvent
    public void onRender(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            if (vertexBuffer == null) {
                return;
            }
            var config = ConfigStore.instance.getConfig().beaconConfig;
            if (!config.enabled || config.entries.size() == 0) {
                return;
            }

            if (mc.level == null) {
                return;
            }

            String dimension = mc.level.dimension().location().toString();
            List<BeaconConfig.BeaconEntry> list = config.entries.stream().filter(e -> dimension.equals(e.dimension)).toList();
            if (list.size() == 0) {
                return;
            }

            Camera camera = event.getCamera();
            Vec3 view = camera.getPosition();

            var tesselator = Tesselator.getInstance();
            var buffer = tesselator.getBuilder();
            buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

            for (var entry: list) {
                float r = entry.color.getRed() / 255f;
                float g = entry.color.getGreen() / 255f;
                float b = entry.color.getBlue() / 255f;
                double x = entry.x;
                double z = entry.z;
                double dx = x - view.x;
                double dz = z - view.z;
                if (dx * dx + dz * dz > MaxRange * MaxRange) {
                    double factor = MaxRange / Math.sqrt(dx * dx + dz * dz);
                    x = view.x + dx * factor;
                    z = view.z + dz * factor;
                }
                buffer.vertex(x, 0, z).color(r, g, b, 1f).endVertex();
                buffer.vertex(x, 512, z).color(r, g, b, 1f).endVertex();
            }

            vertexBuffer.bind();
            vertexBuffer.upload(buffer.end());

            RenderSystem.backupProjectionMatrix();
            double fov = ((GameRendererMixinInterface) mc.gameRenderer).getFov(camera, event.getPartialTick());
            RenderSystem.setProjectionMatrix(getProjectionMatrix(fov));
            // bob/hurt views?

            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableTexture();
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glEnable(GL11.GL_DEPTH_TEST);

            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            poseStack.translate(-view.x, -view.y, -view.z);
            var shader = GameRenderer.getPositionColorShader();
            vertexBuffer.drawWithShader(event.getPoseStack().last().pose(), RenderSystem.getProjectionMatrix(), shader);
            poseStack.popPose();

            VertexBuffer.unbind();

            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableTexture();
            RenderSystem.restoreProjectionMatrix();
        }
    }

    public Matrix4f getProjectionMatrix(double fov) {
        PoseStack posestack = new PoseStack();
        posestack.last().pose().identity();
        /*if (this.zoom != 1.0F) {
            posestack.translate((double)this.zoomX, (double)(-this.zoomY), 0.0D);
            posestack.scale(this.zoom, this.zoom, 1.0F);
        }*/
        // only for screenshots???
        Matrix4f matrix = new Matrix4f();
        matrix.setPerspective((float)(fov * (double)((float)Math.PI / 180F)), (float)mc.getWindow().getWidth() / (float)mc.getWindow().getHeight(), 0.05F, ClipRange);
        posestack.last().pose().mul(matrix);
        return posestack.last().pose();
    }
}