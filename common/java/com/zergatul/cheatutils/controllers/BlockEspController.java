package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.BlockTracerConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.TracerConfigBase;
import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.render.LineRenderer;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

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
                    renderBlockBounding(renderer, pos, config);
                }

                if (config.drawTracers && distanceSqr < tracerMaxDistanceSqr) {
                    drawTracer(
                            renderer,
                            tracerX, tracerY, tracerZ,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            config);
                }
            }
        }

        renderer.end();
    }

    private void renderBlockBounding(LineRenderer renderer, BlockPos pos, BlockTracerConfig config) {
        int x1 = pos.getX();
        int y1 = pos.getY();
        int z1 = pos.getZ();
        int x2 = x1 + 1;
        int y2 = y1 + 1;
        int z2 = z1 + 1;

        float r = config.outlineColor.getRed() / 255f;
        float g = config.outlineColor.getGreen() / 255f;
        float b = config.outlineColor.getBlue() / 255f;

        renderer.cuboid(x1, y1, z1, x2, y2, z2, r, g, b, 1f);
    }

    private void drawTracer(LineRenderer renderer, double tx, double ty, double tz, double x, double y, double z, TracerConfigBase config) {
        float r = config.tracerColor.getRed() / 255f;
        float g = config.tracerColor.getGreen() / 255f;
        float b = config.tracerColor.getBlue() / 255f;

        renderer.line(tx, ty, tz, x, y, z, r, g, b, 1f);
    }
}