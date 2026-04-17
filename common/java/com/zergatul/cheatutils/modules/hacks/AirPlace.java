package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.MouseScrollEvent;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.configs.AirPlaceConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.render.EspLineRenderer;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.cheatutils.utils.MathUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.*;

public class AirPlace implements Module {

    public static final AirPlace instance = new AirPlace();

    private final Minecraft mc = Minecraft.getInstance();
    private boolean active;
    private double range;
    private BlockPos currentPosition;

    private AirPlace() {
        Events.MouseScroll.add(this::onMouseScroll);
        Events.AfterRenderWorld.add(this::onAfterRenderWorld);
    }

    public boolean isActive() {
        return active;
    }

    public void toggle() {
        if (active) {
            disable();
        } else {
            enable();
        }
    }

    public void enable() {
        active = true;
    }

    public void disable() {
        active = false;
    }

    public void onBeforeStartUseItem() {
        if (mc.hitResult == null) {
            return;
        }
        if (currentPosition == null) {
            return;
        }
        if (mc.hitResult.getType() != HitResult.Type.MISS) {
            return;
        }

        mc.hitResult = new BlockHitResult(
                currentPosition.getCenter(),
                Direction.UP,
                currentPosition,
                true);
    }

    private void onMouseScroll(MouseScrollEvent event) {
        if (currentPosition != null) {
            AirPlaceConfig config = ConfigStore.instance.getConfig().airPlaceConfig;
            range += event.getScrollDelta() * 0.5;
            range = MathUtils.clamp(range, config.minRange, config.maxRange);
            event.cancel();
        }
    }

    private void onAfterRenderWorld(RenderWorldLastEvent event) {
        if (!active || mc.level == null || mc.player == null || mc.hitResult == null) {
            reset();
            return;
        }

        if (mc.hitResult.getType() != HitResult.Type.MISS) {
            reset();
            return;
        }

        if (!(mc.player.getMainHandItem().getItem() instanceof BlockItem)) {
            reset();
            return;
        }

        init();

        Camera camera = mc.gameRenderer.mainCamera();
        Quaternionf rotation = new Quaternionf(0, 0, 0, 1);
        rotation.rotationYXZ(
                -camera.yRot() * ((float) Math.PI / 180F),
                camera.xRot() * ((float) Math.PI / 180F),
                0.0F);
        Vector3f direction = new Vector3f().set(0.0F, 0.0F, 1.0F).rotate(rotation).mul((float) range);
        Vec3 pos = camera.position().add(direction.x, direction.y, direction.z);
        BlockPos blockPos = new BlockPos((int) Math.floor(pos.x), (int) Math.floor(pos.y), (int) Math.floor(pos.z));

        double margin = 0.05;
        EspLineRenderer renderer = EspLineRenderer.getInstance();
        renderer.begin();
        renderer.cuboid(
                event.getCameraPos(),
                blockPos.getX() + margin,
                blockPos.getY() + margin,
                blockPos.getZ() + margin,
                blockPos.getX() + 1 - margin,
                blockPos.getY() + 1 - margin,
                blockPos.getZ() + 1 - margin,
                ColorUtils.toShader(Color.WHITE),
                1f);
        renderer.end(event.getMvp(), true);

        currentPosition = blockPos;
    }

    private void init() {
        if (range == 0) {
            // first activation
            AirPlaceConfig config = ConfigStore.instance.getConfig().airPlaceConfig;
            range = (config.minRange + config.maxRange) / 2;
        }
    }

    private void reset() {
        currentPosition = null;
    }
}