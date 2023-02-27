package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.interfaces.LevelChunkMixinInterface;
import com.zergatul.cheatutils.utils.Dimension;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WorldChunk.class)
public abstract class MixinLevelChunk implements LevelChunkMixinInterface {

    @Shadow
    @Final
    private World world;

    private Dimension dimension;

    @Override
    public void onLoad() {
        dimension = Dimension.get((ClientWorld) this.world);
    }

    @Override
    public Dimension getDimension() {
        return dimension;
    }
}