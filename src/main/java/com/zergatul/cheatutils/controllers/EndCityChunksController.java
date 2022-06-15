package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.zergatul.cheatutils.configs.Config;
import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import org.lwjgl.opengl.GL11;

public class EndCityChunksController {

    public static final EndCityChunksController instance = new EndCityChunksController();

    private final Minecraft mc = Minecraft.getInstance();
    private VertexBuffer vertexBuffer;

    private EndCityChunksController() {
        RenderSystem.recordRenderCall(() -> vertexBuffer = new VertexBuffer());
    }

    public void render(RenderLevelLastEvent event) {
        Config config = ConfigStore.instance.getConfig();
        if (!config.esp || !config.endCityChunksConfig.enabled) {
            return;
        }

        if (mc.player == null) {
            return;
        }

        if (mc.player.level.dimension() != Level.END) {
            return;
        }

        Vec3 view = mc.gameRenderer.getMainCamera().getPosition();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (ChunkAccess chunk: ChunkController.instance.getLoadedChunks()) {
            int sx = Math.floorMod(chunk.getPos().x, 20);
            int sz = Math.floorMod(chunk.getPos().z, 20);
            boolean isEndCityChunk = 0 <= sx && sx <= 8 && 0 <= sz && sz <= 8;
            if (isEndCityChunk) {
                int x1 = chunk.getPos().x * 16;
                int z1 = chunk.getPos().z * 16;
                int x2 = x1 + 16;
                int z2 = z1 + 16;
                float r = sx == 4 || sz == 4 ? 1f : 0f;
                float g = 1f;
                float b = 0f;
                for (float y = 32.1f; y < 100; y += 32) {
                    bufferBuilder.vertex(x1, y, z1).color(r, g, b, 0.1f).endVertex();
                    bufferBuilder.vertex(x1, y, z2).color(r, g, b, 0.1f).endVertex();
                    bufferBuilder.vertex(x2, y, z2).color(r, g, b, 0.1f).endVertex();
                    bufferBuilder.vertex(x2, y, z1).color(r, g, b, 0.1f).endVertex();
                }
            }
        }

        vertexBuffer.bind();
        vertexBuffer.upload(bufferBuilder.end());

        PoseStack matrix = event.getPoseStack();
        matrix.pushPose();
        matrix.translate(-view.x, -view.y, -view.z);
        var shader = GameRenderer.getPositionColorShader();
        vertexBuffer.drawWithShader(matrix.last().pose(), event.getProjectionMatrix().copy(), shader);
        matrix.popPose();

        VertexBuffer.unbind();

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

}
