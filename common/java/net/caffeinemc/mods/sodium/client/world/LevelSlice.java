package net.caffeinemc.mods.sodium.client.world;

import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSectionCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class LevelSlice {

    private BoundingBox volume;
    private BlockState[][] blockArrays;
    private int originBlockX, originBlockY, originBlockZ;

    public static ChunkRenderContext prepare(Level level, SectionPos pos, ClonedChunkSectionCache cache) {
        LevelChunk chunk = level.getChunk(pos.getX(), pos.getZ());
        LevelChunkSection section = chunk.getSections()[level.getSectionIndexFromSectionY(pos.getY())];
        if (section == null || section.hasOnlyAir()) {
            return null;
        }
        throw new AssertionError();
    }

    public BlockState getBlockState(BlockPos pos) {
        throw new AssertionError();
    }

    public BlockState getBlockState(int blockX, int blockY, int blockZ) {
        throw new AssertionError();
    }

    public void copyData(ChunkRenderContext context) {
        throw new AssertionError();
    }

    public static int getLocalSectionIndex(int sectionX, int sectionY, int sectionZ) {
        throw new AssertionError();
    }

    public static int getLocalBlockIndex(int blockX, int blockY, int blockZ) {
        throw new AssertionError();
    }
}