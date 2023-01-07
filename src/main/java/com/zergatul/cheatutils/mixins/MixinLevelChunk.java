package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.interfaces.LevelChunkMixinInterface;
import com.zergatul.cheatutils.utils.Dimension;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelChunk.class)
public abstract class MixinLevelChunk implements LevelChunkMixinInterface {

    @Shadow
    @Final
    Level level;

    private long loadTime;
    private Dimension dimension;
    private boolean unloaded;

    @Override
    public long getLoadTime() {
        return loadTime;
    }

    @Override
    public Dimension getDimension() {
        return dimension;
    }

    @Override
    public boolean isUnloaded() {
        return unloaded;
    }

    @Override
    public void onLoad() {
        loadTime = System.nanoTime();
        dimension = Dimension.get((ClientLevel) this.level);
    }

    @Override
    public void onUnload() {
        unloaded = true;
    }
}