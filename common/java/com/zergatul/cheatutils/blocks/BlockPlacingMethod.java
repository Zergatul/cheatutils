package com.zergatul.cheatutils.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public enum BlockPlacingMethod {
    ANY("any"),
    BOTTOM_SLAB("bottom-slab"),
    TOP_SLAB("top-slab"),
    FACING_TOP("facing-top"),
    FACING_BOTTOM("facing-bottom"),
    FACING_EAST("facing-east"),
    FACING_WEST("facing-west"),
    FACING_SOUTH("facing-south"),
    FACING_NORTH("facing-north"),
    FROM_TOP("from-top"),
    FROM_BOTTOM("from-bottom"),
    FROM_HORIZONTAL("from-horizontal"),
    FROM_EAST("from-east"),
    FROM_WEST("from-west"),
    FROM_SOUTH("from-south"),
    FROM_NORTH("from-north"),
    ITEM_USE("item-use"),
    AIR_PLACE("air-place");

    private final String name;

    BlockPlacingMethod(String name) {
        this.name = name;
    }

    public Vec3 getTarget(Vec3 playerPos, BlockPos blockPos, Direction direction, boolean airPlace) {
        return switch (this) {
            case BOTTOM_SLAB -> getBottomSlabTarget(playerPos, blockPos, direction, airPlace);
            case TOP_SLAB -> getTopSlabTarget(playerPos, blockPos, direction, airPlace);
            default -> getTargetDefault(playerPos, blockPos, direction);
        };
    }

    public List<RotationRange> getRotationRanges() {
        final float step = 0.1f;
        return switch (this) {
            case FACING_TOP -> List.of(RotationRange.x(45, 90, step));
            case FACING_BOTTOM -> List.of(RotationRange.x(-90, -45, step));
            case FACING_EAST -> List.of(new RotationRange(-45, 45, 45, 135, step));
            case FACING_WEST -> List.of(new RotationRange(-45, 45, -135, -45, step));
            case FACING_SOUTH -> List.of(new RotationRange(-45, 45, -180, -135, step), new RotationRange(-45, 45, 135, 180, step));
            case FACING_NORTH -> List.of(new RotationRange(-45, 45, -45, 45, step));
            default -> null;
        };
    }

    public boolean isDelayedRotation() {
        return switch (this) {
            case FACING_EAST, FACING_WEST, FACING_SOUTH, FACING_NORTH -> true;
            default -> false;
        };
    }

    public Direction[] getAllowedDirections() {
        return switch (this) {
            case FROM_TOP -> new Direction[] { Direction.DOWN };
            case FROM_HORIZONTAL -> new Direction[] { Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST };
            case FROM_BOTTOM -> new Direction[] { Direction.UP };
            case FROM_EAST -> new Direction[] { Direction.WEST };
            case FROM_WEST -> new Direction[] { Direction.EAST };
            case FROM_SOUTH -> new Direction[] { Direction.NORTH };
            case FROM_NORTH -> new Direction[] { Direction.SOUTH };
            default -> Direction.values();
        };
    }

    public static BlockPlacingMethod facing(Direction direction) {
        return switch (direction) {
            case UP -> FACING_TOP;
            case DOWN -> FACING_BOTTOM;
            case EAST -> FACING_EAST;
            case WEST -> FACING_WEST;
            case NORTH -> FACING_NORTH;
            case SOUTH -> FACING_SOUTH;
        };
    }

    public static BlockPlacingMethod parse(String value) {
        for (BlockPlacingMethod method : values()) {
            if (method.name.equals(value)) {
                return method;
            }
        }
        return ANY;
    }

    private static Vec3 getTargetDefault(Vec3 playerPos, BlockPos blockPos, Direction direction) {
        return new Vec3(
                blockPos.getX() + 0.5f + direction.getOpposite().getStepX() * 0.5,
                blockPos.getY() + 0.5f + direction.getOpposite().getStepY() * 0.5,
                blockPos.getZ() + 0.5f + direction.getOpposite().getStepZ() * 0.5);
    }

    private static Vec3 getBottomSlabTarget(Vec3 playerPos, BlockPos blockPos, Direction direction, boolean airPlace) {
        if (direction == Direction.UP) {
            return !airPlace ? getTargetDefault(playerPos, blockPos, direction) : null;
        } else if (direction == Direction.DOWN) {
            return null;
        } else {
            return new Vec3(
                    blockPos.getX() + 0.5f + direction.getOpposite().getStepX() * 0.5,
                    blockPos.getY() + 0.25f,
                    blockPos.getZ() + 0.5f + direction.getOpposite().getStepZ() * 0.5);
        }
    }

    private static Vec3 getTopSlabTarget(Vec3 playerPos, BlockPos blockPos, Direction direction, boolean airPlace) {
        if (direction == Direction.DOWN) {
            return !airPlace ? getTargetDefault(playerPos, blockPos, direction) : null;
        } else if (direction == Direction.UP) {
            return null;
        } else {
            return new Vec3(
                    blockPos.getX() + 0.5f + direction.getOpposite().getStepX() * 0.5,
                    blockPos.getY() + 0.75f,
                    blockPos.getZ() + 0.5f + direction.getOpposite().getStepZ() * 0.5);
        }
    }
}