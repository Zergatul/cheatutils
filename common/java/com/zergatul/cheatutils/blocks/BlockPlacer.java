package com.zergatul.cheatutils.blocks;

import com.zergatul.cheatutils.configs.BlockPlacerConfig;
import com.zergatul.cheatutils.utils.BlockPlacingMethod;
import com.zergatul.cheatutils.utils.Rotation;
import com.zergatul.cheatutils.utils.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class BlockPlacer {

    private static final Minecraft mc = Minecraft.getInstance();

    public static BlockPlacePlan createPlan(BlockState state, BlockPos pos, BlockPlacingMethod method, BlockPlacerConfig config) {
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;

        if (level == null || player == null) {
            return null;
        }

        if (method == BlockPlacingMethod.ITEM_USE) {
            return new BlockPlacePlan(config, pos, pos, Direction.UP, pos.getCenter());
        }

        BlockState currentState = level.getBlockState(pos);
        if (!currentState.canBeReplaced()) {
            return null;
        }

        CollisionContext collisioncontext = CollisionContext.of(player);
        if (!level.isUnobstructed(state, pos, collisioncontext)) {
            return null;
        }

        if (method != BlockPlacingMethod.AIR_PLACE) {
            for (Direction direction : method.getAllowedDirections()) {
                BlockPos neighbourPos = pos.relative(direction);
                BlockState neighbourState = level.getBlockState(neighbourPos);
                if (!neighbourState.canBeReplaced()) {
                    Vec3 target = method.getTarget(player.getEyePosition(), pos, direction.getOpposite(), false);
                    if (target != null) {
                        Rotation rotation = method.getRotation();
                        if (rotation == null) {
                            if (config.autoRotate) {
                                rotation = RotationUtils.getRotation(player.getEyePosition(), pos.getCenter());
                            }
                        } else {
                            throw new AssertionError();
                        }
                        return new BlockPlacePlan(config, pos, neighbourPos, direction.getOpposite(), target, rotation);
                    }
                }
            }
        }

        if (config.attachToAir) {
            // replaceClicked from BlockPlaceContext
            Vec3 target = method.getTarget(player.getEyePosition(), pos, Direction.UP, true);
            if (target != null) {
                return new BlockPlacePlan(config, pos, pos.immutable(), Direction.UP, target, method.getRotation());
            }
        }

        return null;
    }
}