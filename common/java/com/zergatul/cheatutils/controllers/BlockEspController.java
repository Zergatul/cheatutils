package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.BlockEspConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.EspConfigBase;
import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.render.BlockOverlayRenderer;
import com.zergatul.cheatutils.render.LineRenderer;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
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

        Vec3 playerPos = event.getPlayerPos();
        double playerX = playerPos.x;
        double playerY = playerPos.y;
        double playerZ = playerPos.z;

        Vec3 tracerCenter = event.getTracerCenter();
        double tracerX = tracerCenter.x;
        double tracerY = tracerCenter.y;
        double tracerZ = tracerCenter.z;

        LineRenderer renderer = RenderUtilities.instance.getLineRenderer();
        renderer.begin(event, false);

        BlockOverlayRenderer overlayRenderer = RenderUtilities.instance.getBlockOverlayRenderer();

        List<BlockPos> overlayList = new ArrayList<>();
        for (BlockEspConfig config: ConfigStore.instance.getConfig().blocks.getBlockConfigs()) {
            if (!config.enabled) {
                continue;
            }

            Set<BlockPos> set = BlockFinderController.instance.blocks.get(config);
            if (set == null) {
                continue;
            }

            double tracerMaxDistanceSqr = config.getTracerMaxDistanceSqr();
            double outlineMaxDistanceSqr = config.getOutlineMaxDistanceSqr();
            double overlayMaxDistanceSqr = config.getOverlayMaxDistanceSqr();

            overlayList.clear();
            for (BlockPos pos: set) {
                double dx = pos.getX() - playerX;
                double dy = pos.getY() - playerY;
                double dz = pos.getZ() - playerZ;
                double distanceSqr = dx * dx + dy * dy + dz * dz;

                if (config.drawOutline && distanceSqr < outlineMaxDistanceSqr) {
                    renderBlockBounding(renderer, pos, config);
                }

                if (config.drawTracers && distanceSqr < tracerMaxDistanceSqr) {
                    drawTracer(
                            renderer,
                            tracerX, tracerY, tracerZ,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            config);
                }

                if (config.drawOverlay && distanceSqr < overlayMaxDistanceSqr) {
                    overlayList.add(pos);
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

        renderer.end();
    }

    private void renderBlockBounding(LineRenderer renderer, BlockPos pos, BlockEspConfig config) {
        int x1 = pos.getX();
        int y1 = pos.getY();
        int z1 = pos.getZ();
        int x2 = x1 + 1;
        int y2 = y1 + 1;
        int z2 = z1 + 1;

        float r = config.outlineColor.getRed() / 255f;
        float g = config.outlineColor.getGreen() / 255f;
        float b = config.outlineColor.getBlue() / 255f;
        float a = config.outlineColor.getAlpha() / 255f;

        renderer.cuboid(x1, y1, z1, x2, y2, z2, r, g, b, a);
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

    private void drawTracer(LineRenderer renderer, double tx, double ty, double tz, double x, double y, double z, EspConfigBase config) {
        float r = config.tracerColor.getRed() / 255f;
        float g = config.tracerColor.getGreen() / 255f;
        float b = config.tracerColor.getBlue() / 255f;
        float a = config.tracerColor.getAlpha() / 255f;

        renderer.line(tx, ty, tz, x, y, z, r, g, b, a);
    }
}