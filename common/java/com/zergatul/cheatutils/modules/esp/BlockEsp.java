package com.zergatul.cheatutils.modules.esp;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.configs.BlockEspConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.render.BlockOverlayRenderer;
import com.zergatul.cheatutils.render.LineRenderer;
import com.zergatul.cheatutils.render.ScreenSpaceLineRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlockEsp {

    public static final BlockEsp instance = new BlockEsp();

    private final List<BlockPos> overlayList = new ArrayList<>();

    private BlockEsp() {
        Events.RenderWorldLast.add(this::render);
        Events.Close.add(() -> RenderUtilities.instance.getBlockOverlayRenderer().close());
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

        RenderUtilities utilities = RenderUtilities.instance;
        LineRenderer thinRenderer = utilities.getLineRenderer();
        ScreenSpaceLineRenderer thickRenderer = utilities.getScreenSpaceLineRenderer();
        BlockOverlayRenderer overlayRenderer = utilities.getBlockOverlayRenderer();
        thinRenderer.begin(event, false);
        thickRenderer.begin(event);

        for (BlockEspConfig config : ConfigStore.instance.getConfig().blocks.getBlockConfigs()) {
            if (!config.enabled) {
                continue;
            }

            Set<BlockPos> set = BlockFinder.instance.blocks.get(config);
            if (set == null || set.isEmpty()) {
                continue;
            }

            double tracerMaxDistanceSqr = config.getTracerMaxDistanceSqr();
            double boundingBoxMaxDistanceSqr = config.getBoundingBoxMaxDistanceSqr();
            double overlayMaxDistanceSqr = config.getOverlayMaxDistanceSqr();

            Color boundingBoxColor = config.boundingBoxColor;
            float boundingBoxR = color(boundingBoxColor.getRed());
            float boundingBoxG = color(boundingBoxColor.getGreen());
            float boundingBoxB = color(boundingBoxColor.getBlue());
            float boundingBoxA = color(boundingBoxColor.getAlpha());

            Color tracerColor = config.tracerColor;
            float tracerR = color(tracerColor.getRed());
            float tracerG = color(tracerColor.getGreen());
            float tracerB = color(tracerColor.getBlue());
            float tracerA = color(tracerColor.getAlpha());

            overlayList.clear();
            for (BlockPos pos : set) {
                double dx = pos.getX() - playerX;
                double dy = pos.getY() - playerY;
                double dz = pos.getZ() - playerZ;
                double distanceSqr = dx * dx + dy * dy + dz * dz;

                if (config.drawBoundingBox && distanceSqr < boundingBoxMaxDistanceSqr) {
                    renderBoundingBox(
                            thinRenderer,
                            thickRenderer,
                            pos,
                            (float) config.boundingBoxWidth,
                            boundingBoxR,
                            boundingBoxG,
                            boundingBoxB,
                            boundingBoxA);
                }

                if (config.drawTracers && distanceSqr < tracerMaxDistanceSqr) {
                    renderTracer(
                            thinRenderer,
                            thickRenderer,
                            tracerX,
                            tracerY,
                            tracerZ,
                            pos,
                            (float) config.tracerWidth,
                            tracerR,
                            tracerG,
                            tracerB,
                            tracerA);
                }

                if (config.drawOverlay && distanceSqr < overlayMaxDistanceSqr) {
                    overlayList.add(pos);
                }
            }

            if (!overlayList.isEmpty()) {
                overlayRenderer.begin(event);
                for (BlockPos pos : overlayList) {
                    overlayRenderer.cuboid(
                            pos.getX(), pos.getY(), pos.getZ(),
                            pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
                }
                overlayRenderer.end(config.overlayColor);
            }
        }

        thinRenderer.end();
        thickRenderer.end();
    }

    private void renderBoundingBox(
            LineRenderer thinRenderer,
            ScreenSpaceLineRenderer thickRenderer,
            BlockPos pos,
            float width,
            float r, float g, float b, float a) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        if (width == 1) {
            thinRenderer.cuboid(x, y, z, x + 1, y + 1, z + 1, r, g, b, a);
        } else {
            thickRenderer.cuboid(x, y, z, x + 1, y + 1, z + 1, width, r, g, b, a);
        }
    }

    private void renderTracer(
            LineRenderer thinRenderer,
            ScreenSpaceLineRenderer thickRenderer,
            double x, double y, double z,
            BlockPos pos,
            float width,
            float r, float g, float b, float a) {
        double targetX = pos.getX() + 0.5;
        double targetY = pos.getY() + 0.5;
        double targetZ = pos.getZ() + 0.5;
        if (width == 1) {
            thinRenderer.line(x, y, z, targetX, targetY, targetZ, r, g, b, a);
        } else {
            thickRenderer.line(x, y, z, targetX, targetY, targetZ, width, r, g, b, a);
        }
    }

    private static float color(int value) {
        return value / 255f;
    }
}