package com.zergatul.cheatutils.scripting.types;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

@SuppressWarnings("unused")
@CustomType(name = "BlockPos")
public class BlockPosWrapper {

    private final int x;
    private final int y;
    private final int z;

    public BlockPosWrapper(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockPosWrapper(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Getter(name = "x")
    public int getX() {
        return x;
    }

    @Getter(name = "y")
    public int getY() {
        return y;
    }

    @Getter(name = "z")
    public int getZ() {
        return z;
    }

    public BlockPosWrapper above() {
        return relative(Direction.UP);
    }

    public BlockPosWrapper above(int offset) {
        return relative(Direction.UP, offset);
    }

    public BlockPosWrapper below() {
        return relative(Direction.DOWN);
    }

    public BlockPosWrapper below(int offset) {
        return relative(Direction.DOWN, offset);
    }

    public BlockPosWrapper north() {
        return relative(Direction.NORTH);
    }

    public BlockPosWrapper north(int offset) {
        return relative(Direction.NORTH, offset);
    }

    public BlockPosWrapper south() {
        return relative(Direction.SOUTH);
    }

    public BlockPosWrapper south(int offset) {
        return relative(Direction.SOUTH, offset);
    }

    public BlockPosWrapper east() {
        return relative(Direction.EAST);
    }

    public BlockPosWrapper east(int offset) {
        return relative(Direction.EAST, offset);
    }

    public BlockPosWrapper west() {
        return relative(Direction.WEST);
    }

    public BlockPosWrapper west(int offset) {
        return relative(Direction.WEST, offset);
    }

    private BlockPosWrapper relative(Direction direction) {
        return relative(direction, 1);
    }

    private BlockPosWrapper relative(Direction direction, int offset) {
        return new BlockPosWrapper(
                x + direction.getStepX() * offset,
                y + direction.getStepY() * offset,
                z + direction.getStepZ() * offset);
    }
}