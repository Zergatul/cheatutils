package me.jellysquid.mods.sodium.client.world;

import me.jellysquid.mods.sodium.client.world.cloned.ChunkRenderContext;
import me.jellysquid.mods.sodium.client.world.cloned.ClonedChunkSectionCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class WorldSlice {
    private final BlockState[][] blockArrays = new BlockState[27][];
    private int originX;
    private int originY;
    private int originZ;
    private BoundingBox volume;

    public WorldSlice(ClientLevel level) {}

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