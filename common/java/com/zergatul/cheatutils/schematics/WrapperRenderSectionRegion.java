package com.zergatul.cheatutils.schematics;

import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class WrapperRenderSectionRegion implements BlockAndTintGetter {

    private final Level level;
    private final BlockAndTintGetter inner;
    private final SchematicaSectionCopy[] schematicaSections;
    private final int minSectionX;
    private final int minSectionY;
    private final int minSectionZ;

    public WrapperRenderSectionRegion(
            Level level,
            BlockAndTintGetter inner,
            SchematicaSectionCopy[] schematicaSections,
            int minSectionX,
            int minSectionY,
            int minSectionZ
    ) {
        this.level = level;
        this.inner = inner;
        this.schematicaSections = schematicaSections;
        this.minSectionX = minSectionX;
        this.minSectionY = minSectionY;
        this.minSectionZ = minSectionZ;
    }

    @Override
    public float getShade(Direction direction, boolean p_45523_) {
        return level.getShade(direction, p_45523_);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return inner.getLightEngine();
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver resolver) {
        return level.getBlockTint(pos, resolver);
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

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int xs = SectionPos.blockToSectionCoord(x);
        int ys = SectionPos.blockToSectionCoord(y);
        int zs = SectionPos.blockToSectionCoord(z);

        int index = RenderSectionRegion.index(minSectionX, minSectionY, minSectionZ, xs, ys, zs);
        return schematicaSections[index].getBlockState(x, y, z);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public int getHeight() {
        throw new IllegalStateException();
    }

    @Override
    public int getMinY() {
        throw new IllegalStateException();
    }
}