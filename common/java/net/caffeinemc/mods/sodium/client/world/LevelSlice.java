package net.caffeinemc.mods.sodium.client.world;

import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSectionCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.chunk.LevelChunkSection;

/**
 * Compile-time stub for optional Sodium integration. This class is excluded from produced jars.
 */
public final class LevelSlice {

    private final BlockState[][] blockArrays = null;
    private int originBlockX;
    private int originBlockY;
    private int originBlockZ;
    private BoundingBox volume;

    public LevelSlice(ClientLevel level) {}

    public static ChunkRenderContext prepare(Level level, SectionPos pos, ClonedChunkSectionCache cache) {
        var section = new LevelChunkSection(level.palettedContainerFactory());
        section.hasOnlyAir();
        return null;
    }

    public void copyData(ChunkRenderContext context) {}

    public BlockState getBlockState(int x, int y, int z) {
        return Blocks.AIR.defaultBlockState();
    }

    public void reset() {}
}