package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.controllers.CoordinatesLeakProtectionController;
import com.zergatul.cheatutils.interfaces.LevelChunkMixinInterface;
import com.zergatul.cheatutils.utils.Dimension;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

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

    @Inject(at = @At("TAIL"), method = "replaceWithPacketData(Lnet/minecraft/network/FriendlyByteBuf;Lnet/minecraft/nbt/CompoundTag;Ljava/util/function/Consumer;)V")
    private void onAfterReplaceWithPacketData(FriendlyByteBuf p_187972_, CompoundTag p_187973_, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> p_187974_, CallbackInfo ci) {
        CoordinatesLeakProtectionController.instance.processChunk((LevelChunk) (Object) this);
    }
}