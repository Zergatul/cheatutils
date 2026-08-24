package com.zergatul.cheatutils.modules.esp;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.configs.BlockEspConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.render.BlockOverlayRenderer;
import com.zergatul.cheatutils.render.InstancedCubeLineRenderer;
import com.zergatul.cheatutils.render.InstancedTracerRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class BlockEsp {

    public static final BlockEsp instance = new BlockEsp();

    private BlockEsp() {
        Events.RenderWorldLast.add(this::render);
        Events.Close.add(this::close);
    }

    private void render(RenderWorldLastEvent event) {
        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }

        Vec3 playerPos = event.getPlayerPos();
        double playerX = playerPos.x;
        double playerY = playerPos.y;
        double playerZ = playerPos.z;

        Vec3 cameraPos = event.getCamera().getPosition();
        double cameraX = cameraPos.x;
        double cameraY = cameraPos.y;
        double cameraZ = cameraPos.z;

        RenderUtilities utilities = RenderUtilities.instance;
        InstancedCubeLineRenderer cubeRenderer = utilities.getInstancedCubeLineRenderer();
        InstancedTracerRenderer tracerRenderer = utilities.getInstancedTracerRenderer();
        BlockOverlayRenderer overlayRenderer = utilities.getBlockOverlayRenderer();
        cubeRenderer.begin();
        tracerRenderer.begin();

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

            if (config.drawOverlay) {
                overlayRenderer.begin(event);
            }

            for (BlockPos pos : set) {
                double dx = pos.getX() - playerX;
                double dy = pos.getY() - playerY;
                double dz = pos.getZ() - playerZ;
                double distanceSqr = dx * dx + dy * dy + dz * dz;

                if (config.drawBoundingBox && distanceSqr < boundingBoxMaxDistanceSqr) {
                    cubeRenderer.cube(
                            (float) (pos.getX() - cameraX),
                            (float) (pos.getY() - cameraY),
                            (float) (pos.getZ() - cameraZ),
                            config.boundingBoxColor,
                            (float) config.boundingBoxWidth);
                }

                if (config.drawTracers && distanceSqr < tracerMaxDistanceSqr) {
                    tracerRenderer.tracer(
                            (float) (pos.getX() + 0.5 - cameraX),
                            (float) (pos.getY() + 0.5 - cameraY),
                            (float) (pos.getZ() + 0.5 - cameraZ),
                            config.tracerColor,
                            (float) config.tracerWidth);
                }

                if (config.drawOverlay && distanceSqr < overlayMaxDistanceSqr) {
                    overlayRenderer.block(pos.getX(), pos.getY(), pos.getZ());
                }
            }

            if (config.drawOverlay) {
                overlayRenderer.end(config.overlayColor);
            }
        }

        tracerRenderer.end(event);
        cubeRenderer.end(event);
    }

    private void close() {
        RenderUtilities utilities = RenderUtilities.instance;
        utilities.getInstancedCubeLineRenderer().close();
        utilities.getInstancedTracerRenderer().close();
        utilities.getBlockOverlayRenderer().close();
    }
}