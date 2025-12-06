package com.zergatul.cheatutils.scripting.types;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("unused")
@CustomType(name = "HitResult")
public class HitResultWrapper {

    private final HitResult result;

    public HitResultWrapper() {
        this(BlockHitResult.miss(Vec3.ZERO, Direction.DOWN, BlockPos.ZERO));
    }

    public HitResultWrapper(HitResult result) {
        this.result = result;
    }

    @Getter(name = "isMiss")
    public boolean getIsMiss() {
        return result.getType() == HitResult.Type.MISS;
    }

    @Getter(name = "hasBlock")
    public boolean getHasBlock() {
        return result.getType() == HitResult.Type.BLOCK;
    }

    @Getter(name = "hasEntity")
    public boolean getHasEntity() {
        return result.getType() == HitResult.Type.ENTITY;
    }

    @Getter(name = "blockPos")
    public BlockPosWrapper getBlockPos() {
        if (result.getType() == HitResult.Type.BLOCK) {
            return new BlockPosWrapper(((BlockHitResult) result).getBlockPos());
        } else {
            return new BlockPosWrapper(0, 0, 0);
        }
    }

    @Getter(name = "entityId")
    public int getEntityId() {
        if (result.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult) result).getEntity().getId();
        } else {
            return Integer.MIN_VALUE;
        }
    }

    @Getter(name = "location")
    public Position3d getLocation() {
        return new Position3d(result.getLocation());
    }
}