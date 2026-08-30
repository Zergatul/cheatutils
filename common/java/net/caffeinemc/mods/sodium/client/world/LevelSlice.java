package net.caffeinemc.mods.sodium.client.world;

import net.caffeinemc.mods.sodium.client.world.cloned.ChunkRenderContext;
import net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSectionCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class LevelSlice {
    private final BlockState[][] blockArrays = new BlockState[27][];
    private int originBlockX;
    private int originBlockY;
    private int originBlockZ;
    private BoundingBox volume;

    public LevelSlice(ClientLevel level) {}

    public static ChunkRenderContext prepare(Level level, SectionPos pos, ClonedChunkSectionCache cache) {
        LevelChunkSection section = null;
        if (section == null || section.hasOnlyAir()) {
            return null;
        }
        return null;
    }

    public void copyData(ChunkRenderContext context) {}

    public BlockState getBlockState(int x, int y, int z) {
        return Blocks.AIR.defaultBlockState();
    }

    public void reset() {}
}