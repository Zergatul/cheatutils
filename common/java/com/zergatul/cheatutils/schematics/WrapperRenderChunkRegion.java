package com.zergatul.cheatutils.schematics;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public class WrapperRenderChunkRegion implements BlockAndTintGetter {

    public static final int RADIUS = 1;
    public static final int SIZE = RADIUS * 2 + 1;

    private final BlockAndTintGetter inner;
    private final SchematicaSectionCopy[] schematicaSections;
    private final int minSectionX;
    private final int minSectionY;
    private final int minSectionZ;

    public WrapperRenderChunkRegion(
            BlockAndTintGetter inner,
            SchematicaSectionCopy[] schematicaSections,
            int minSectionX,
            int minSectionY,
            int minSectionZ
    ) {
        this.inner = inner;
        this.schematicaSections = schematicaSections;
        this.minSectionX = minSectionX;
        this.minSectionY = minSectionY;
        this.minSectionZ = minSectionZ;
    }

    @Override
    public float getShade(Direction direction, boolean shade) {
        return inner.getShade(direction, shade);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return inner.getLightEngine();
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver resolver) {
        return inner.getBlockTint(pos, resolver);
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        BlockState state = inner.getBlockState(pos);
        if (!state.isAir()) {
            return state;
        }

        int sectionX = SectionPos.blockToSectionCoord(pos.getX());
        int sectionY = SectionPos.blockToSectionCoord(pos.getY());
        int sectionZ = SectionPos.blockToSectionCoord(pos.getZ());
        if (sectionX < minSectionX || sectionX >= minSectionX + SIZE ||
                sectionY < minSectionY || sectionY >= minSectionY + SIZE ||
                sectionZ < minSectionZ || sectionZ >= minSectionZ + SIZE) {
            return state;
        }

        return schematicaSections[index(minSectionX, minSectionY, minSectionZ, sectionX, sectionY, sectionZ)]
                .getBlockState(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public int getMinBuildHeight() {
        return inner.getMinBuildHeight();
    }

    @Override
    public int getHeight() {
        return inner.getHeight();
    }

    public static int index(int minSectionX, int minSectionY, int minSectionZ, int sectionX, int sectionY, int sectionZ) {
        return sectionX - minSectionX + (sectionY - minSectionY) * SIZE + (sectionZ - minSectionZ) * SIZE * SIZE;
    }
}