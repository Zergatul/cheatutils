package com.zergatul.cheatutils.modules.hacks;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.configs.AreaMineConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.mixins.common.accessors.ClientLevelAccessor;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.render.LineRenderer;
import com.zergatul.cheatutils.render.Primitives;
import com.zergatul.cheatutils.wrappers.PickRange;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class AreaMine implements Module {

    public static final AreaMine instance = new AreaMine();

    private final Minecraft mc = Minecraft.getInstance();

    private AreaMine() {
        Events.RenderWorldLast.add(this::onRenderWorldLast);
        Events.BeforeInstaMine.add(this::onBeforeInstaMine);
    }

    private void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }

        AreaMineConfig config = ConfigStore.instance.getConfig().areaMineConfig;
        if (!config.enabled || !config.preview) {
            return;
        }

        if (mc.player == null || mc.level == null) {
            return;
        }

        Entity entity = mc.getCameraEntity();
        if (entity == null) {
            return;
        }

        HitResult result = entity.pick(PickRange.get(), 0.0F, false);
        if (result.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos blockPos = ((BlockHitResult) result).getBlockPos();
        BlockState blockState = mc.level.getBlockState(blockPos);
        boolean instamine = blockState.getDestroyProgress(mc.player, mc.player.level(), blockPos) >= 1;
        if (!instamine) {
            return;
        }

        LineRenderer renderer = RenderUtilities.instance.getLineRenderer();
        renderer.begin(event, false);

        int time = (int) (System.currentTimeMillis() % 2000);
        if (time > 1000) {
            time = 2000 - time;
        }
        double brad = 0.2 + 0.2 * time / 1000;
        forEachInstaminable(event.getPlayerPos(), blockPos, config, pos -> {
            double x1 = pos.getX() + 0.5 - brad;
            double x2 = pos.getX() + 0.5 + brad;
            double y1 = pos.getY() + 0.5 - brad;
            double y2 = pos.getY() + 0.5 + brad;
            double z1 = pos.getZ() + 0.5 - brad;
            double z2 = pos.getZ() + 0.5 + brad;
            renderer.cuboid(x1, y1, z1, x2, y2, z2, 1f, 1f, 1f, 1f);
        });

        renderer.end();
    }

    private void onBeforeInstaMine(BlockPos origin) {
        AreaMineConfig config = ConfigStore.instance.getConfig().areaMineConfig;
        if (!config.enabled) {
            return;
        }

        if (mc.player == null || mc.level == null) {
            return;
        }

        forEachInstaminable(mc.player.getPosition(1f), origin, config, pos -> {
            if (pos.equals(origin)) {
                return;
            }

            BlockStatePredictionHandler handler = ((ClientLevelAccessor) mc.level).getBlockStatePredictionHandler_CU();
            handler.startPredicting();
            // we call startPredicting inside another startPredicting which is not ok
            // but original close() method should be called and fix it

            ServerboundPlayerActionPacket packet = new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                    pos,
                    Direction.UP,
                    handler.currentSequence());

            NetworkPacketsController.instance.sendPacket(packet);
        });
    }

    private void forEachInstaminable(Vec3 playerPos, BlockPos origin, AreaMineConfig config, Consumer<BlockPos> consumer) {
        double pick2 = PickRange.get();
        pick2 = pick2 * pick2;
        int delta = (int) Math.floor(config.radius);
        int r2 = (int) Math.floor(config.radius * config.radius);
        for (int x = -delta; x <= delta; x++) {
            for (int y = -delta; y <= delta; y++) {
                for (int z = -delta; z <= delta; z++) {
                    if (x * x + y * y + z * z <= r2) {
                        BlockPos pos = origin.offset(x, y, z);
                        if (pos.distToCenterSqr(playerPos) > pick2) {
                            continue;
                        }

                        BlockState state = mc.level.getBlockState(pos);
                        if (state.isAir()) {
                            continue;
                        }

                        boolean instamine = state.getDestroyProgress(mc.player, mc.player.level(), pos) >= 1;
                        if (instamine) {
                            consumer.accept(pos);
                        }
                    }
                }
            }
        }
    }
}