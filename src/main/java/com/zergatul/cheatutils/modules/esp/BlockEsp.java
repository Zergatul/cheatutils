package com.zergatul.cheatutils.modules.esp;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.BlockEspConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.modules.esp.blocks.BlockFinder;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.render.BlockEspOverlayRenderer;
import com.zergatul.cheatutils.render.EspCubeLineRender;
import com.zergatul.cheatutils.render.TracerRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.*;
import java.util.List;

public class BlockEsp implements Module {

    public static final BlockEsp INSTANCE = new BlockEsp();

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

    private void render(RenderWorldLastEvent event) {
        if (!enabled || !EspGlobal.enabled) {
            return;
        }

        renderConfiguredEntries(event);
    }

    private void renderConfiguredEntries(RenderWorldLastEvent event) {
        ImmutableList<BlockEspConfig> configs = ConfigStore.instance.getConfig().blocks.getBlockConfigs();
        if (configs.isEmpty()) {
            return;
        }

        Vec3d playerPos = event.getPlayerPos();
        double playerX = playerPos.x;
        double playerY = playerPos.y;
        double playerZ = playerPos.z;

        TracerRenderer tracerRenderer = TracerRenderer.INSTANCE;
        tracerRenderer.begin();
        EspCubeLineRender cubeRenderer = EspCubeLineRender.INSTANCE;
        cubeRenderer.begin();

        for (BlockEspConfig config : configs) {
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

            bbList.clear();
            tracerList.clear();
            overlayList.clear();

            for (BlockPos pos : set) {
                double dx = pos.getX() - playerX;
                double dy = pos.getY() - playerY;
                double dz = pos.getZ() - playerZ;
                double distanceSqr = dx * dx + dy * dy + dz * dz;

                if (config.drawBoundingBox && distanceSqr < boundingBoxMaxDistanceSqr) {
                    bbList.add(pos);
                }

                if (config.drawTracers && distanceSqr < tracerMaxDistanceSqr) {
                    tracerList.add(pos);
                }

                if (config.drawOverlay && distanceSqr < overlayMaxDistanceSqr) {
                    overlayList.add(pos);
                }
            }

            if (!bbList.isEmpty()) {
                renderBoundingBoxes(cubeRenderer, (float) config.boundingBoxWidth, config.boundingBoxColor.getRGB(), event);
            }

            if (!tracerList.isEmpty()) {
                renderTracers(tracerRenderer, (float) config.tracerWidth, config.tracerColor.getRGB(), event);
            }

            if (!overlayList.isEmpty()) {
                renderOverlay(config.overlayColor, event);
            }
        }

        tracerRenderer.end(event.getMvp());
        cubeRenderer.end(event.getMvp());
    }

    private void renderBoundingBoxes(EspCubeLineRender renderer, float width, int color, RenderWorldLastEvent event) {
        Vec3d cameraPos = event.getCameraPos();
        double cameraX = cameraPos.x;
        double cameraY = cameraPos.y;
        double cameraZ = cameraPos.z;

        for (BlockPos pos : bbList) {
            double x = pos.getX();
            double y = pos.getY();
            double z = pos.getZ();
            renderer.cube(
                    (float) (x - cameraX),
                    (float) (y - cameraY),
                    (float) (z - cameraZ),
                    color,
                    width);
        }
    }

    private void renderTracers(TracerRenderer renderer, float width, int color, RenderWorldLastEvent event) {
        Vec3d cameraPos = event.getCameraPos();
        double cameraX = cameraPos.x;
        double cameraY = cameraPos.y;
        double cameraZ = cameraPos.z;

        for (BlockPos pos : tracerList) {
            renderer.tracer(
                    (float) (pos.getX() + 0.5 - cameraX),
                    (float) (pos.getY() + 0.5 - cameraY),
                    (float) (pos.getZ() + 0.5 - cameraZ),
                    color, width);
        }
    }

    private void renderOverlay(Color color, RenderWorldLastEvent event) {
        Vec3d cameraPos = event.getCameraPos();
        double cameraX = cameraPos.x;
        double cameraY = cameraPos.y;
        double cameraZ = cameraPos.z;

        BlockEspOverlayRenderer renderer = BlockEspOverlayRenderer.getInstance();
        renderer.begin();
        for (BlockPos pos : overlayList) {
            renderer.cube(
                    (float) (pos.getX() - cameraX),
                    (float) (pos.getY() - cameraY),
                    (float) (pos.getZ() - cameraZ));
        }
        renderer.end(event.getMvp(), color);
    }
}