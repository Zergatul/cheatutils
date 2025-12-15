package com.zergatul.cheatutils.modules.esp;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.BlockEspConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.render.*;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.scripting.modules.BlockEspEvent;
import com.zergatul.cheatutils.scripting.types.BlockPosWrapper;
import com.zergatul.cheatutils.utils.ColorUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class BlockEsp {

    public static final BlockEsp instance = new BlockEsp();

    private final List<CustomBlockPosEntry> customEntries = new ArrayList<>();
    private final List<BlockPos> bbList = new ArrayList<>();
    private final List<BlockPos> tracerList = new ArrayList<>();
    private final List<BlockPos> overlayList = new ArrayList<>();
    private boolean enabled = true;

    private BlockEsp() {
        Events.AfterRenderWorld.add(this::render);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }

    public void addCustom(BlockPos pos, int color) {
        customEntries.removeIf(e -> e.pos.equals(pos));
        customEntries.add(new CustomBlockPosEntry(pos.immutable(), color));
    }

    public void clearCustom() {
        customEntries.clear();
    }

    public void removeCustom(BlockPos pos) {
        customEntries.removeIf(e -> e.pos.equals(pos));
    }

    private void render(RenderWorldLastEvent event) {
        if (!enabled || !EspGlobal.enabled) {
            return;
        }

        if (!customEntries.isEmpty()) {
            final float shift = 0.01f;
            Vec3 view = event.getCamera().position();
            Color3dRenderer renderer = RenderUtilities.instance.getColor3dRenderer();
            renderer.begin();
            for (CustomBlockPosEntry entry : customEntries) {
                renderer.cuboid(
                        (float) (entry.pos.getX() - view.x - shift),
                        (float) (entry.pos.getY() - view.y - shift),
                        (float) (entry.pos.getZ() - view.z - shift),
                        (float) (entry.pos.getX() - view.x + 1 + shift),
                        (float) (entry.pos.getY() - view.y + 1 + shift),
                        (float) (entry.pos.getZ() - view.z + 1 + shift),
                        ColorUtils.r(entry.color), ColorUtils.g(entry.color), ColorUtils.b(entry.color), ColorUtils.a(entry.color));
            }
            GlStateManager._depthMask(false);
            renderer.end(event.getMvp());
            GlStateManager._depthMask(true);
        }

        Vec3 playerPos = event.getPlayerPos();
        double playerX = playerPos.x;
        double playerY = playerPos.y;
        double playerZ = playerPos.z;

        Vec3 tracerCenter = event.getTracerCenter();
        double tracerX = tracerCenter.x;
        double tracerY = tracerCenter.y;
        double tracerZ = tracerCenter.z;

        GroupLineRenderer lineRenderer = RenderUtilities.instance.getGroupLineRenderer();
        GroupThickLineRenderer thickLineRenderer = RenderUtilities.instance.getGroupThickLineRenderer();
        BlockOverlayRenderer overlayRenderer = RenderUtilities.instance.getBlockOverlayRenderer();

        for (BlockEspConfig config : ConfigStore.instance.getConfig().blocks.getBlockConfigs()) {
            if (!config.enabled) {
                continue;
            }

            Set<BlockPos> set = BlockFinder.instance.blocks.get(config);
            if (set == null || set.isEmpty()) {
                continue;
            }

            double tracerMaxDistanceSqr = config.getTracerMaxDistanceSqr();
            double outlineMaxDistanceSqr = config.getOutlineMaxDistanceSqr();
            double overlayMaxDistanceSqr = config.getOverlayMaxDistanceSqr();

            bbList.clear();
            tracerList.clear();
            overlayList.clear();

            if (config.scriptEnabled && config.script != null) {
                BlockScriptResult result = new BlockScriptResult();
                BlockEspEvent blockEspEvent = new BlockEspEvent(result);
                for (BlockPos pos : set) {
                    double dx = pos.getX() - playerX;
                    double dy = pos.getY() - playerY;
                    double dz = pos.getZ() - playerZ;
                    double distanceSqr = dx * dx + dy * dy + dz * dz;

                    if (distanceSqr >= outlineMaxDistanceSqr && distanceSqr >= tracerMaxDistanceSqr && distanceSqr >= overlayMaxDistanceSqr) {
                        continue;
                    }

                    result.reset();
                    config.script.accept(new BlockPosWrapper(pos), blockEspEvent);

                    if (distanceSqr < outlineMaxDistanceSqr && result.shouldDrawOutline(config.drawOutline)) {
                        bbList.add(pos);
                    }

                    if (distanceSqr < tracerMaxDistanceSqr && result.shouldDrawTracer(config.drawTracers)) {
                        tracerList.add(pos);
                    }

                    if (distanceSqr < overlayMaxDistanceSqr && result.shouldDrawOverlay(config.drawOverlay)) {
                        overlayList.add(pos);
                    }
                }
            } else {
                for (BlockPos pos : set) {
                    double dx = pos.getX() - playerX;
                    double dy = pos.getY() - playerY;
                    double dz = pos.getZ() - playerZ;
                    double distanceSqr = dx * dx + dy * dy + dz * dz;

                    if (config.drawOutline && distanceSqr < outlineMaxDistanceSqr) {
                        bbList.add(pos);
                    }

                    if (config.drawTracers && distanceSqr < tracerMaxDistanceSqr) {
                        tracerList.add(pos);
                    }

                    if (config.drawOverlay && distanceSqr < overlayMaxDistanceSqr) {
                        overlayList.add(pos);
                    }
                }
            }

            if (!bbList.isEmpty()) {
                final int lineWidth = config.outlineWidth;
                CuboidRenderer renderer;
                if (lineWidth == 1) {
                    lineRenderer.begin(event);
                    renderer = lineRenderer;
                } else {
                    thickLineRenderer.begin(event, lineWidth);
                    renderer = thickLineRenderer;
                }

                for (BlockPos pos : bbList) {
                    double x = pos.getX();
                    double y = pos.getY();
                    double z = pos.getZ();
                    renderer.cuboid(x, y, z, x + 1, y + 1, z + 1);
                }

                if (lineWidth == 1) {
                    lineRenderer.end(config.outlineColor);
                } else {
                    thickLineRenderer.end(config.outlineColor);
                }
            }

            if (!tracerList.isEmpty()) {
                final int lineWidth = config.tracerWidth;
                SimpleLineRenderer renderer;
                if (lineWidth == 1) {
                    lineRenderer.begin(event);
                    renderer = lineRenderer;
                } else {
                    thickLineRenderer.begin(event, lineWidth);
                    renderer = thickLineRenderer;
                }

                for (BlockPos pos : tracerList) {
                    renderer.line(
                            tracerX, tracerY, tracerZ,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                }

                if (lineWidth == 1) {
                    lineRenderer.end(config.tracerColor);
                } else {
                    thickLineRenderer.end(config.tracerColor);
                }
            }

            if (!overlayList.isEmpty()) {
                overlayRenderer.begin(event);
                for (BlockPos pos: overlayList) {
                    renderOverlay(overlayRenderer, pos);
                }
                overlayRenderer.end(
                        config.overlayColor.getRed() / 255f,
                        config.overlayColor.getGreen() / 255f,
                        config.overlayColor.getBlue() / 255f,
                        config.overlayColor.getAlpha() / 255f);
            }
        }
    }

    private void renderOverlay(BlockOverlayRenderer renderer, BlockPos pos) {
        int x1 = pos.getX();
        int y1 = pos.getY();
        int z1 = pos.getZ();
        int x2 = x1 + 1;
        int y2 = y1 + 1;
        int z2 = z1 + 1;

        // bottom
        renderer.quad(
                x2, y1, z2,
                x1, y1, z2,
                x1, y1, z1,
                x2, y1, z1);

        // top
        renderer.quad(
                x2, y2, z2,
                x2, y2, z1,
                x1, y2, z1,
                x1, y2, z2);

        // west
        renderer.quad(
                x1, y1, z1,
                x1, y1, z2,
                x1, y2, z2,
                x1, y2, z1);

        // east
        renderer.quad(
                x2, y1, z1,
                x2, y2, z1,
                x2, y2, z2,
                x2, y1, z2);

        // north
        renderer.quad(
                x2, y1, z1,
                x1, y1, z1,
                x1, y2, z1,
                x2, y2, z1);

        // south
        renderer.quad(
                x2, y1, z2,
                x2, y2, z2,
                x1, y2, z2,
                x1, y1, z2);
    }

    public static class BlockScriptResult {

        public int tracer;
        public int outline;
        public int overlay;

        public void reset() {
            tracer = -1;
            outline = -1;
            overlay = -1;
        }

        public boolean shouldDrawTracer(boolean setting) {
            if (tracer == -1) {
                return setting;
            }
            return tracer != 0;
        }

        public boolean shouldDrawOutline(boolean setting) {
            if (outline == -1) {
                return setting;
            }
            return outline != 0;
        }

        public boolean shouldDrawOverlay(boolean setting) {
            if (overlay == -1) {
                return setting;
            }
            return overlay != 0;
        }
    }

    private record CustomBlockPosEntry(BlockPos pos, int color) {}
}