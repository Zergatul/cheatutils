package net.caffeinemc.mods.sodium.client.render.chunk;

import net.minecraft.world.level.chunk.LevelChunkSection;

public class RenderSectionManager {
    public void onSectionAdded(int x, int y, int z) {
        LevelChunkSection section = null;
        section.hasOnlyAir();
    }
}