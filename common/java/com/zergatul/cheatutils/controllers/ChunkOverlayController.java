package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.chunkoverlays.*;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.*;
import com.zergatul.cheatutils.render.Position2dTextureColorRenderer;
import com.zergatul.cheatutils.utils.Dimension;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.LevelChunk;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChunkOverlayController {

    public static final ChunkOverlayController instance = new ChunkOverlayController();

    // store texture of 16x16 chunks
    private static final int SegmentSize = 16;
    // 250ms
    private static final long UpdateDelay = 250L * 1000000;
    private static final float MinScale = 1 * SegmentSize;
    private static final float MaxScale = 32 * SegmentSize;
    private static final float ScaleStep = 1.3f;

    private final Minecraft mc = Minecraft.getInstance();
    private final List<AbstractChunkOverlay> overlays = new ArrayList<>();
    private float scale = 16 * SegmentSize;

    private ChunkOverlayController() {
        register(new ExplorationMiniMapChunkOverlay(SegmentSize, UpdateDelay));
        register(new NewChunksOverlay(SegmentSize, UpdateDelay));
        register(new WorldDownloadChunkOverlay(SegmentSize, UpdateDelay));

        Events.RawChunkLoaded.add(this::onChunkLoaded);
        Events.RawBlockUpdated.add(this::onBlockChanged);
        Events.AfterRenderWorld.add(this::render, 30);
        Events.PreRenderGuiOverlay.add(this::onPreRenderGameOverlay);
        Events.MouseScroll.add(this::onMouseScroll);
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractChunkOverlay> T ofType(Class<T> clazz) {
        return (T) overlays.stream().filter(o -> o.getClass() == clazz).findFirst().orElse(null);
    }

    private void render(RenderWorldLastEvent event) {
        if (noOverlaysEnabled()) {
            return;
        }

        for (AbstractChunkOverlay overlay: overlays) {
            overlay.onPreRender();
        }

        if (!mc.options.keyPlayerList.isDown()) {
            return;
        }

        if (mc.hasAltDown()) {
            return;
        }

        if (mc.player == null || mc.level == null) {
            return;
        }

        float frameTime = event.getPartialTickTime();
        float xp = (float) Mth.lerp(frameTime, mc.player.xo, mc.player.getX());
        float zp = (float) Mth.lerp(frameTime, mc.player.zo, mc.player.getZ());
        float xc = (float) mc.gameRenderer.mainCamera().position().x;
        float zc = (float) mc.gameRenderer.mainCamera().position().z;
        float yRot = mc.gameRenderer.mainCamera().yRot();

        Matrix4f matrix = new Matrix4f();
        matrix.ortho(
                -1f * mc.getWindow().getGuiScaledWidth() / 2,
                +1f * mc.getWindow().getGuiScaledWidth() / 2,
                +1f * mc.getWindow().getGuiScaledHeight() / 2,
                -1f * mc.getWindow().getGuiScaledHeight() / 2,
                -1, 1);

        Quaternionf quaternion = new Quaternionf(0, 0, 0, 1);
        quaternion.rotationYXZ(-(float)Math.PI, -(float)Math.PI, -yRot * ((float)Math.PI / 180F));
        matrix.rotate(quaternion);

        float multiplier = 1f / (16 * SegmentSize) * scale;
        Dimension dimension = Dimension.get(mc.level);

        try (Position2dTextureColorRenderer.BufferBuilder buffer = new Position2dTextureColorRenderer.BufferBuilder()) {
            for (AbstractChunkOverlay overlay : overlays) {
                for (Segment segment : overlay.getSegments(dimension)) {
                    if (segment.texture == null) {
                        continue;
                    }

                    float x = (segment.pos.x * 16 * SegmentSize - xc) * multiplier;
                    float y = (segment.pos.z * 16 * SegmentSize - zc) * multiplier;

                    buffer.clear();
                    buffer.rect(x, y, scale, scale, Color.WHITE.getRGB());
                    Position2dTextureColorRenderer.getInstance().draw(
                            mc.gameRenderer.mainRenderTarget(),
                            segment.texture.getTextureView(),
                            matrix,
                            buffer);
                }
            }
        }

        for (AbstractChunkOverlay overlay: overlays) {
            overlay.onPostDrawSegments(dimension, matrix, xp, zp, xc, zc, multiplier);
        }
    }

    private void onPreRenderGameOverlay(PreRenderGuiOverlayEvent event) {
        if (event.getGuiOverlayType() == PreRenderGuiOverlayEvent.GuiOverlayType.PLAYER_LIST) {
            if (noOverlaysEnabled()) {
                return;
            }
            if (mc.hasAltDown()) {
                return;
            }
            event.cancel();
        }
    }

    private void onMouseScroll(MouseScrollEvent event) {
        if (noOverlaysEnabled()) {
            return;
        }

        if (!mc.options.keyPlayerList.isDown()) {
            return;
        }

        event.cancel();

        if (event.getScrollDelta() >= 1.0d) {
            if (scale < MaxScale) {
                scale *= ScaleStep;
            }
        }

        if (event.getScrollDelta() <= -1.0d) {
            if (scale > MinScale) {
                scale /= ScaleStep;
            }
        }
    }

    private void register(AbstractChunkOverlay overlay) {
        overlays.add(overlay);
    }

    private void onChunkLoaded(LevelChunk chunk) {
        for (AbstractChunkOverlay overlay : overlays) {
            overlay.onChunkLoaded(chunk);
        }
    }

    private void onBlockChanged(BlockUpdateEvent event) {
        assert mc.level != null;

        Dimension dimension = Dimension.get(mc.level);
        for (AbstractChunkOverlay overlay : overlays) {
            overlay.onBlockChanged(dimension, event.pos(), event.state());
        }
    }

    private boolean noOverlaysEnabled() {
        return overlays.stream().noneMatch(AbstractChunkOverlay::isEnabled);
    }
}