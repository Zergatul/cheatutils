package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.BlockTracerConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.TracerConfigBase;
import com.zergatul.cheatutils.utils.SharedVertexBuffer;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.Set;

public class BlockEspController {

    public static final BlockEspController instance = new BlockEspController();

    private BlockEspController() {
        Events.RenderWorldLast.add(this::render);
    }

    private void render(RenderWorldLastEvent event) {
        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }

        Vec3 view = event.getCamera().getPosition();

        Vec3 playerPos = event.getPlayerPos();
        double playerX = playerPos.x;
        double playerY = playerPos.y;
        double playerZ = playerPos.z;

        Vec3 tracerCenter = event.getTracerCenter();
        double tracerX = tracerCenter.x;
        double tracerY = tracerCenter.y;
        double tracerZ = tracerCenter.z;

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        for (BlockTracerConfig config: ConfigStore.instance.getConfig().blocks.configs) {
            if (!config.enabled) {
                continue;
            }

            Set<BlockPos> set = BlockFinderController.instance.blocks.get(config.block);
            if (set == null) {
                continue;
            }

            double tracerMaxDistanceSqr = config.getTracerMaxDistanceSqr();
            double outlineMaxDistanceSqr = config.getOutlineMaxDistanceSqr();

            for (BlockPos pos: set) {
                double dx = pos.getX() - playerX;
                double dy = pos.getY() - playerY;
                double dz = pos.getZ() - playerZ;
                double distanceSqr = dx * dx + dy * dy + dz * dz;

                if (config.drawOutline && distanceSqr < outlineMaxDistanceSqr) {
                    renderBlockBounding(buffer, view, pos, config);
                }

                if (config.drawTracers && distanceSqr < tracerMaxDistanceSqr) {
                    drawTracer(
                            buffer,
                            view,
                            tracerX, tracerY, tracerZ,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            config);
                }
            }
        }

        SharedVertexBuffer.instance.bind();
        SharedVertexBuffer.instance.upload(buffer.end());

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        SharedVertexBuffer.instance.drawWithShader(event.getMatrixStack().last().pose(), event.getProjectionMatrix(), GameRenderer.getPositionColorShader());
        VertexBuffer.unbind();

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableTexture();
        RenderSystem.enableDepthTest();
    }

    private static void renderBlockBounding(BufferBuilder buffer, Vec3 view, BlockPos pos, BlockTracerConfig config) {
        int x1 = pos.getX();
        int y1 = pos.getY();
        int z1 = pos.getZ();
        int x2 = x1 + 1;
        int y2 = y1 + 1;
        int z2 = z1 + 1;

        float r = config.outlineColor.getRed() / 255f;
        float g = config.outlineColor.getGreen() / 255f;
        float b = config.outlineColor.getBlue() / 255f;

        // bottom
        buffer.vertex(x1 - view.x, y1 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1 - view.x, y1 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1 - view.x, y1 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y1 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y1 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y1 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y1 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1 - view.x, y1 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();

        // top
        buffer.vertex(x1 - view.x, y2 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1 - view.x, y2 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1 - view.x, y2 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y2 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y2 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y2 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y2 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1 - view.x, y2 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();

        // side
        buffer.vertex(x1 - view.x, y1 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1 - view.x, y2 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1 - view.x, y1 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x1 - view.x, y2 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y1 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y2 - view.y, z2 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y1 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x2 - view.x, y2 - view.y, z1 - view.z).color(r, g, b, 1f).endVertex();
    }

    private static void drawTracer(BufferBuilder buffer, Vec3 view, double tx, double ty, double tz, double x, double y, double z, TracerConfigBase config) {
        float r = config.tracerColor.getRed() / 255f;
        float g = config.tracerColor.getGreen() / 255f;
        float b = config.tracerColor.getBlue() / 255f;

        buffer.vertex(tx - view.x, ty - view.y, tz - view.z).color(r, g, b, 1f).endVertex();
        buffer.vertex(x - view.x, y - view.y, z - view.z).color(r, g, b, 1f).endVertex();
    }
}